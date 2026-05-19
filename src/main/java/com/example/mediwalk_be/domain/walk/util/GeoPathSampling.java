package com.example.mediwalk_be.domain.walk.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 경로 폴리라인(위경도 순)을 따라 일정 간격으로 좌표를 샘플링합니다.
 */
public final class GeoPathSampling {

	private GeoPathSampling() {
	}

	public static List<double[]> sampleAlongPath(List<double[]> latLonPairs, double intervalMeters, int maxSamples) {
		if (latLonPairs == null || latLonPairs.isEmpty() || maxSamples <= 0) {
			return List.of();
		}
		List<double[]> out = new ArrayList<>(Math.min(maxSamples, latLonPairs.size() + 8));
		out.add(new double[] { latLonPairs.getFirst()[0], latLonPairs.getFirst()[1] });
		if (maxSamples <= 1) {
			return out;
		}
		if (intervalMeters <= 10) {
			return capCopy(latLonPairs, maxSamples);
		}

		double total = pathLengthMeters(latLonPairs);
		if (total <= intervalMeters * 0.5) {
			addIfFar(out, latLonPairs.getLast(), 25);
			return stripNearDuplicates(out, 15);
		}

		double nextEmit = intervalMeters;
		double cumBefore = 0.0;

		for (int i = 0; i < latLonPairs.size() - 1; i++) {
			double[] a = latLonPairs.get(i);
			double[] b = latLonPairs.get(i + 1);
			double segLen = DistanceUtil.calculateDistanceMeters(a[0], a[1], b[0], b[1]);

			while (out.size() < maxSamples && nextEmit <= total + 1e-3 && segLen > 1e-6
					&& nextEmit <= cumBefore + segLen + 1e-3) {
				double frac = (nextEmit - cumBefore) / segLen;
				if (frac < 0 || frac > 1 + 1e-9) {
					break;
				}
				if (frac > 1) {
					frac = 1;
				}
				double lat = a[0] + (b[0] - a[0]) * frac;
				double lon = a[1] + (b[1] - a[1]) * frac;
				out.add(new double[] { lat, lon });
				nextEmit += intervalMeters;
			}

			cumBefore += segLen;
			if (out.size() >= maxSamples) {
				break;
			}
		}

		double[] last = latLonPairs.getLast();
		if (out.size() < maxSamples) {
			addIfFar(out, last, (int) (intervalMeters * 0.25));
		}

		return stripNearDuplicates(out, 15);
	}

	private static void addIfFar(List<double[]> out, double[] pt, double minGapMeters) {
		double[] prev = out.get(out.size() - 1);
		double d = DistanceUtil.calculateDistanceMeters(prev[0], prev[1], pt[0], pt[1]);
		if (d >= minGapMeters) {
			out.add(new double[] { pt[0], pt[1] });
		}
	}

	private static List<double[]> capCopy(List<double[]> latLonPairs, int maxSamples) {
		List<double[]> r = new ArrayList<>(Math.min(maxSamples, latLonPairs.size()));
		for (int i = 0; i < latLonPairs.size() && r.size() < maxSamples; i++) {
			double[] p = latLonPairs.get(i);
			r.add(new double[] { p[0], p[1] });
		}
		return r;
	}

	private static double pathLengthMeters(List<double[]> path) {
		double sum = 0;
		for (int i = 0; i < path.size() - 1; i++) {
			double[] a = path.get(i);
			double[] b = path.get(i + 1);
			sum += DistanceUtil.calculateDistanceMeters(a[0], a[1], b[0], b[1]);
		}
		return sum;
	}

	private static List<double[]> stripNearDuplicates(List<double[]> points, double minMeters) {
		if (points.size() <= 1) {
			return points;
		}
		List<double[]> r = new ArrayList<>(points.size());
		double[] prev = null;
		for (double[] p : points) {
			if (prev != null && DistanceUtil.calculateDistanceMeters(prev[0], prev[1], p[0], p[1]) < minMeters) {
				continue;
			}
			r.add(p);
			prev = p;
		}
		return r;
	}
}
