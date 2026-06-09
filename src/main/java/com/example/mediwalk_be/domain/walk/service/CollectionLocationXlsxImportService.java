package com.example.mediwalk_be.domain.walk.service;

import com.example.mediwalk_be.domain.walk.dto.response.CollectionLocationImportResponse;
import com.example.mediwalk_be.domain.walk.entity.CollectionLocation;
import com.example.mediwalk_be.domain.walk.entity.enums.CollectionLocationType;
import com.example.mediwalk_be.domain.walk.repository.CollectionLocationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CollectionLocationXlsxImportService {

	private static final int NAME_MAX_LENGTH = 100;
	private static final int ADDRESS_MAX_LENGTH = 255;
	private static final int BATCH_SIZE = 100;
	private static final Pattern DONG_PATTERN = Pattern.compile("([가-힣0-9]+(?:동|읍|면|리))");
	private static final Pattern PAREN_SUFFIX_PATTERN = Pattern.compile("\\(([^)]+)\\)\\s*$");
	private static final Pattern DISTRICT_PREFIX_PATTERN = Pattern.compile("^서울특별시\\s*([가-힣]+구)\\s*");

	private final CollectionLocationRepository collectionLocationRepository;
	private final DataFormatter dataFormatter = new DataFormatter();

	@Transactional
	public CollectionLocationImportResponse importFromXlsx(InputStream inputStream, String filename) {
		return importFromXlsx(inputStream, filename, false);
	}

	@Transactional
	public CollectionLocationImportResponse importFromXlsx(
			InputStream inputStream,
			String filename,
			boolean refreshNamesOnly) {
		List<ParsedRow> parsedRows = parseRows(inputStream);

		if (refreshNamesOnly) {
			return refreshNames(parsedRows, filename);
		}

		Set<String> existingCoordKeys = loadExistingCoordKeys();
		Set<String> seenInBatch = new HashSet<>();

		int skippedInvalid = 0;
		int skippedDuplicate = 0;
		List<CollectionLocation> toInsert = new ArrayList<>();

		for (ParsedRow row : parsedRows) {
			CollectionLocation entity = toEntity(row);
			if (entity == null) {
				skippedInvalid++;
				continue;
			}

			String coordKey = coordKey(entity.getLatitude(), entity.getLongitude());
			if (!seenInBatch.add(coordKey)) {
				skippedDuplicate++;
				continue;
			}
			if (existingCoordKeys.contains(coordKey)) {
				skippedDuplicate++;
				continue;
			}

			toInsert.add(entity);
			existingCoordKeys.add(coordKey);
		}

		saveInBatches(toInsert);

		String source = sourceLabel(filename);
		return new CollectionLocationImportResponse(
				source,
				parsedRows.size(),
				skippedInvalid,
				skippedDuplicate,
				toInsert.size(),
				0,
				(int) collectionLocationRepository.count());
	}

	private CollectionLocationImportResponse refreshNames(List<ParsedRow> parsedRows, String filename) {
		Map<String, CollectionLocation> byCoord = loadLocationsByCoord();
		int skippedInvalid = 0;
		int skippedMissing = 0;
		int unchanged = 0;
		int updated = 0;

		for (ParsedRow row : parsedRows) {
			String name = buildName(row);
			if (name.isBlank()) {
				skippedInvalid++;
				continue;
			}

			CollectionLocation location = byCoord.get(coordKey(row.latitude(), row.longitude()));
			if (location == null) {
				skippedMissing++;
				continue;
			}
			if (name.equals(location.getName())) {
				unchanged++;
				continue;
			}

			location.changeName(name);
			updated++;
		}

		return new CollectionLocationImportResponse(
				sourceLabel(filename) + " (names)",
				parsedRows.size(),
				skippedInvalid,
				unchanged + skippedMissing,
				0,
				updated,
				(int) collectionLocationRepository.count());
	}

	private List<ParsedRow> parseRows(InputStream inputStream) {
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
			if (sheet == null) {
				throw new IllegalArgumentException("엑셀 시트가 비어 있습니다.");
			}

			Map<String, Integer> columns = resolveColumns(sheet);
			int useCol = columns.getOrDefault("use", 1);
			int nameCol = columns.getOrDefault("name", 2);
			int subCategoryCol = columns.getOrDefault("subCategory", 3);
			int districtCol = columns.getOrDefault("district", 5);
			int roadAddressCol = columns.getOrDefault("roadAddress", 6);
			int lotAddressCol = columns.getOrDefault("lotAddress", 7);
			int languageCol = columns.getOrDefault("language", 9);
			int longitudeCol = columns.getOrDefault("longitude", 10);
			int latitudeCol = columns.getOrDefault("latitude", 11);

			int headerRowIndex = columns.getOrDefault("headerRow", 0);
			List<ParsedRow> rows = new ArrayList<>();

			for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row row = sheet.getRow(rowIndex);
				if (row == null) {
					continue;
				}

				String use = cellText(row, useCol);
				if (!"Y".equalsIgnoreCase(use)) {
					continue;
				}

				String language = cellText(row, languageCol);
				if (!language.isBlank() && !"KOR".equalsIgnoreCase(language)) {
					continue;
				}

				String longitudeText = cellText(row, longitudeCol);
				String latitudeText = cellText(row, latitudeCol);
				if (longitudeText.isBlank() || latitudeText.isBlank()) {
					continue;
				}

				rows.add(new ParsedRow(
						cellText(row, nameCol),
						cellText(row, subCategoryCol),
						cellText(row, districtCol),
						cellText(row, roadAddressCol),
						cellText(row, lotAddressCol),
						parseCoordinate(latitudeText),
						parseCoordinate(longitudeText)));
			}

			return rows;
		} catch (IOException ex) {
			throw new IllegalStateException("엑셀 파일을 읽지 못했습니다.", ex);
		}
	}

	private Map<String, Integer> resolveColumns(Sheet sheet) {
		for (int rowIndex = 0; rowIndex <= Math.min(5, sheet.getLastRowNum()); rowIndex++) {
			Row row = sheet.getRow(rowIndex);
			if (row == null) {
				continue;
			}

			Map<String, Integer> columns = new HashMap<>();
			for (Cell cell : row) {
				String header = normalizeHeader(cellText(row, cell.getColumnIndex()));
				if (header.contains("사용유무")) {
					columns.put("use", cell.getColumnIndex());
				} else if (header.contains("콘텐츠명")) {
					columns.put("name", cell.getColumnIndex());
				} else if (header.contains("서브카테고리")) {
					columns.put("subCategory", cell.getColumnIndex());
				} else if (header.equals("구명") || header.contains("구명")) {
					columns.put("district", cell.getColumnIndex());
				} else if (header.contains("새주소") || header.contains("도로명")) {
					columns.put("roadAddress", cell.getColumnIndex());
				} else if (header.contains("지번주소")) {
					columns.put("lotAddress", cell.getColumnIndex());
				} else if (header.contains("다국어")) {
					columns.put("language", cell.getColumnIndex());
				} else if (header.contains("좌표[x]") || header.equals("좌표x")) {
					columns.put("longitude", cell.getColumnIndex());
				} else if (header.contains("좌표[y]") || header.equals("좌표y")) {
					columns.put("latitude", cell.getColumnIndex());
				}
			}

			if (columns.containsKey("latitude") && columns.containsKey("longitude")) {
				columns.put("headerRow", rowIndex);
				return columns;
			}
		}

		Map<String, Integer> defaults = new HashMap<>();
		defaults.put("headerRow", 0);
		defaults.put("use", 1);
		defaults.put("name", 2);
		defaults.put("subCategory", 3);
		defaults.put("district", 5);
		defaults.put("roadAddress", 6);
		defaults.put("lotAddress", 7);
		defaults.put("language", 9);
		defaults.put("longitude", 10);
		defaults.put("latitude", 11);
		return defaults;
	}

	private CollectionLocation toEntity(ParsedRow row) {
		String address = truncate(firstNonBlank(row.roadAddress(), row.lotAddress()), ADDRESS_MAX_LENGTH);
		if (address.isBlank()) {
			return null;
		}

		String name = buildName(row);
		if (name.isBlank()) {
			return null;
		}

		return CollectionLocation.builder()
				.name(name)
				.address(address)
				.latitude(row.latitude())
				.longitude(row.longitude())
				.type(resolveType(row.subCategory(), name))
				.baseRewardAmount(3000)
				.activationRadius(20)
				.build();
	}

	private String buildName(ParsedRow row) {
		String district = normalizeDistrict(row.district(), row.roadAddress(), row.lotAddress());
		String subCategory = meaningfulSubCategory(row.subCategory());
		String road = row.roadAddress().trim();
		String jibun = row.lotAddress().trim();

		String building = extractBuildingName(road);
		if (!building.isBlank()) {
			return truncate(joinNonBlank(district, building, "폐의약품 수거함"), NAME_MAX_LENGTH);
		}

		String dong = extractDongName(jibun);
		if (!dong.isBlank()) {
			return truncate(joinNonBlank(district, dong, subCategory, "폐의약품 수거함"), NAME_MAX_LENGTH);
		}

		String roadLabel = extractRoadLabel(road, district);
		if (!roadLabel.isBlank()) {
			return truncate(joinNonBlank(district, roadLabel, subCategory, "폐의약품 수거함"), NAME_MAX_LENGTH);
		}

		String baseName = row.contentName().trim();
		if (!baseName.isBlank() && !"폐의약품 수거함".equals(baseName)) {
			return truncate(joinNonBlank(district, baseName), NAME_MAX_LENGTH);
		}

		return truncate(joinNonBlank(district, subCategory, "폐의약품 수거함"), NAME_MAX_LENGTH);
	}

	private static String meaningfulSubCategory(String subCategory) {
		if (subCategory == null || subCategory.isBlank() || "기타".equals(subCategory.trim())) {
			return "";
		}
		return subCategory.trim();
	}

	private static String normalizeDistrict(String district, String roadAddress, String lotAddress) {
		if (district != null && !district.isBlank()) {
			return district.trim();
		}
		Matcher matcher = DISTRICT_PREFIX_PATTERN.matcher(firstNonBlank(roadAddress, lotAddress));
		if (matcher.find()) {
			return matcher.group(1);
		}
		return "";
	}

	private static String extractBuildingName(String roadAddress) {
		if (roadAddress == null || roadAddress.isBlank()) {
			return "";
		}
		Matcher matcher = PAREN_SUFFIX_PATTERN.matcher(roadAddress.trim());
		if (!matcher.find()) {
			return "";
		}
		String inside = matcher.group(1).trim();
		if (inside.isBlank()) {
			return "";
		}
		String[] parts = inside.split(",");
		return parts[parts.length - 1].trim();
	}

	private static String extractDongName(String lotAddress) {
		if (lotAddress == null || lotAddress.isBlank()) {
			return "";
		}
		Matcher matcher = DONG_PATTERN.matcher(lotAddress);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return "";
	}

	private static String extractRoadLabel(String roadAddress, String district) {
		if (roadAddress == null || roadAddress.isBlank()) {
			return "";
		}
		String trimmed = roadAddress.trim();
		Matcher matcher = DISTRICT_PREFIX_PATTERN.matcher(trimmed);
		if (matcher.find()) {
			trimmed = trimmed.substring(matcher.end()).trim();
		} else if (district != null && !district.isBlank() && trimmed.startsWith(district)) {
			trimmed = trimmed.substring(district.length()).trim();
		}
		int comma = trimmed.indexOf(',');
		if (comma > 0) {
			trimmed = trimmed.substring(0, comma).trim();
		}
		return trimmed;
	}

	private static String joinNonBlank(String... parts) {
		StringBuilder builder = new StringBuilder();
		for (String part : parts) {
			if (part == null || part.isBlank()) {
				continue;
			}
			if (!builder.isEmpty()) {
				builder.append(' ');
			}
			builder.append(part.trim());
		}
		return builder.toString();
	}

	private String cellText(Row row, int columnIndex) {
		if (row == null) {
			return "";
		}
		Cell cell = row.getCell(columnIndex);
		if (cell == null) {
			return "";
		}
		String value = dataFormatter.formatCellValue(cell);
		return value != null ? value.trim() : "";
	}

	private static String normalizeHeader(String header) {
		return header.replace('\n', ' ')
				.replaceAll("\\s+", "")
				.toLowerCase(Locale.ROOT);
	}

	private static double parseCoordinate(String value) {
		return Double.parseDouble(value.replace(",", "").trim());
	}

	private static String firstNonBlank(String first, String second) {
		if (first != null && !first.isBlank()) {
			return first.trim();
		}
		return second != null ? second.trim() : "";
	}

	private Set<String> loadExistingCoordKeys() {
		Set<String> keys = new HashSet<>();
		for (CollectionLocation location : collectionLocationRepository.findAll()) {
			keys.add(coordKey(location.getLatitude(), location.getLongitude()));
		}
		return keys;
	}

	private Map<String, CollectionLocation> loadLocationsByCoord() {
		Map<String, CollectionLocation> byCoord = new HashMap<>();
		for (CollectionLocation location : collectionLocationRepository.findAll()) {
			byCoord.put(coordKey(location.getLatitude(), location.getLongitude()), location);
		}
		return byCoord;
	}

	private void saveInBatches(List<CollectionLocation> entities) {
		for (int i = 0; i < entities.size(); i += BATCH_SIZE) {
			int end = Math.min(i + BATCH_SIZE, entities.size());
			collectionLocationRepository.saveAll(entities.subList(i, end));
		}
	}

	private static CollectionLocationType resolveType(String subCategory, String name) {
		String normalized = ((subCategory != null ? subCategory : "") + " " + (name != null ? name : ""))
				.toLowerCase(Locale.ROOT);
		if (normalized.contains("보건소") || normalized.contains("보건지소")) {
			return CollectionLocationType.HEALTH_CENTER;
		}
		if (normalized.contains("약국")) {
			return CollectionLocationType.PHARMACY;
		}
		return CollectionLocationType.COMMUNITY_CENTER;
	}

	private static String coordKey(double latitude, double longitude) {
		return roundCoord(latitude) + "|" + roundCoord(longitude);
	}

	private static String roundCoord(double value) {
		return String.format(Locale.ROOT, "%.6f", value);
	}

	private static String sourceLabel(String filename) {
		return filename != null && !filename.isBlank() ? filename.trim() : "xlsx";
	}

	private static String truncate(String value, int maxLength) {
		if (value == null) {
			return "";
		}
		String trimmed = value.trim();
		if (trimmed.length() <= maxLength) {
			return trimmed;
		}
		return trimmed.substring(0, maxLength);
	}

	private record ParsedRow(
			String contentName,
			String subCategory,
			String district,
			String roadAddress,
			String lotAddress,
			double latitude,
			double longitude
	) {
	}
}
