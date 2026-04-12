package com.example.mediwalk_be.flyway;

import com.example.mediwalk_be.domain.mission.entity.enums.AchievementCategory;
import com.example.mediwalk_be.domain.mission.entity.enums.AchievementCode;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.api.migration.JavaMigration;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;

@Component
public class V1__Achievement_master_seed implements JavaMigration {

	@Override
	public MigrationVersion getVersion() {
		return MigrationVersion.fromVersion("1");
	}

	@Override
	public String getDescription() {
		return "Achievement master codes and default catalog rows";
	}

	@Override
	public boolean canExecuteInTransaction() {
		return true;
	}

	@Override
	public Integer getChecksum() {
		return null;
	}

	@Override
	public void migrate(Context context) throws Exception {
		Connection connection = context.getConnection();
		String catalog = connection.getCatalog();
		DatabaseMetaData md = connection.getMetaData();

		boolean achievementsTableExists = tableExists(md, catalog, "achievements");
		if (!achievementsTableExists) {
			try (Statement st = connection.createStatement()) {
				st.execute("""
						CREATE TABLE achievements (
						  id BIGINT NOT NULL AUTO_INCREMENT,
						  code VARCHAR(64) NOT NULL,
						  name VARCHAR(100) NOT NULL,
						  description VARCHAR(255) NOT NULL,
						  category VARCHAR(30) NOT NULL,
						  target_value INT NOT NULL,
						  unit VARCHAR(10) NOT NULL,
						  icon_type VARCHAR(30) NULL,
						  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
						  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
						  PRIMARY KEY (id),
						  CONSTRAINT uk_achievements_code UNIQUE (code)
						) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
						""");
			}
		} else if (!columnExists(md, catalog, "achievements", "code")) {
			try (Statement st = connection.createStatement()) {
				st.execute("ALTER TABLE achievements ADD COLUMN code VARCHAR(64) NULL");
				st.execute("UPDATE achievements SET code = CONCAT('LEGACY_', id) WHERE code IS NULL");
				st.execute("ALTER TABLE achievements MODIFY code VARCHAR(64) NOT NULL");
				if (!uniqueIndexOnCode(md, catalog, "achievements")) {
					st.execute("CREATE UNIQUE INDEX uk_achievements_code ON achievements(code)");
				}
			}
		}

		upsertCatalog(connection);
	}

	private static void upsertCatalog(Connection connection) throws Exception {
		String sql = """
				INSERT INTO achievements (code, name, description, category, target_value, unit, icon_type, created_at, updated_at)
				VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
				ON DUPLICATE KEY UPDATE
				  name = VALUES(name),
				  description = VALUES(description),
				  category = VALUES(category),
				  target_value = VALUES(target_value),
				  unit = VALUES(unit),
				  icon_type = VALUES(icon_type),
				  updated_at = CURRENT_TIMESTAMP(6)
				""";
		try (var ps = connection.prepareStatement(sql)) {
			for (AchievementCode ac : AchievementCode.values()) {
				ps.setString(1, ac.getDbCode());
				ps.setString(2, ac.getDisplayName());
				ps.setString(3, ac.getDescription());
				ps.setString(4, categoryToColumn(ac.getCategory()));
				ps.setInt(5, ac.getTargetValue());
				ps.setString(6, ac.getUnit());
				ps.setString(7, ac.getIconType());
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	private static String categoryToColumn(AchievementCategory c) {
		return c.name();
	}

	private static boolean tableExists(DatabaseMetaData md, String catalog, String table) throws Exception {
		String[] types = {"TABLE"};
		for (String t : new String[] {table, table.toUpperCase(Locale.ROOT)}) {
			try (ResultSet rs = md.getTables(catalog, null, t, types)) {
				if (rs.next()) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean columnExists(DatabaseMetaData md, String catalog, String table, String column) throws Exception {
		try (ResultSet rs = md.getColumns(catalog, null, table, column)) {
			return rs.next();
		}
	}

	private static boolean uniqueIndexOnCode(DatabaseMetaData md, String catalog, String table) throws Exception {
		try (ResultSet rs = md.getIndexInfo(catalog, null, table, true, false)) {
			while (rs.next()) {
				String col = rs.getString("COLUMN_NAME");
				if (col != null && col.equalsIgnoreCase("code") && !rs.getBoolean("NON_UNIQUE")) {
					return true;
				}
			}
		}
		return false;
	}
}
