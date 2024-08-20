package io.deeplay.camp.botfarm.bots.denis_bots;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KMeans {

    private static final Random random = new Random();

    public static List<Cluster> kMeansCluster(List<ClastPlaceExpMaxBot.UtilityMoveResult> points, int k, int maxIterations) {
        List<Cluster> clusters = initializeClusters(points, k);

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            for (Cluster cluster : clusters) {
                cluster.points.clear();
            }
            for (ClastPlaceExpMaxBot.UtilityMoveResult point : points) {
                Cluster closestCluster = findClosestCluster(clusters, point);
                closestCluster.points.add(point);
            }

            boolean centroidsChanged = false;
            for (Cluster cluster : clusters) {
                ClastPlaceExpMaxBot.UtilityMoveResult newCentroid = calculateCentroid(cluster.points);
                if (!equals(cluster.centroid, newCentroid)) {
                    cluster.centroid = newCentroid;
                    centroidsChanged = true;
                }
            }

            if (!centroidsChanged) {
                break;
            }
        }

        return clusters;
    }

    private static List<Cluster> initializeClusters(List<ClastPlaceExpMaxBot.UtilityMoveResult> points, int k) {
        List<Cluster> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            ClastPlaceExpMaxBot.UtilityMoveResult centroid = points.get(random.nextInt(points.size()));
            clusters.add(new Cluster(centroid));
        }
        return clusters;
    }

    private static Cluster findClosestCluster(List<Cluster> clusters, ClastPlaceExpMaxBot.UtilityMoveResult point) {
        Cluster closestCluster = null;
        double minDistance = Double.MAX_VALUE;
        for (Cluster cluster : clusters) {
            double distance = calculateDistance(cluster.centroid, point);
            if (distance < minDistance) {
                minDistance = distance;
                closestCluster = cluster;
            }
        }
        return closestCluster;
    }

    private static ClastPlaceExpMaxBot.UtilityMoveResult calculateCentroid(List<ClastPlaceExpMaxBot.UtilityMoveResult> points) {
        if (points.isEmpty()) {
            return randomCentroid();
        }

        double sum = 0;
        for (ClastPlaceExpMaxBot.UtilityMoveResult point : points) {
            sum += point.value;
        }
        double average = sum / points.size();

        return new ClastPlaceExpMaxBot.UtilityMoveResult(average, null); // null для MakeMoveEvent, так как мы считаем только значение
    }

    private static double calculateDistance(ClastPlaceExpMaxBot.UtilityMoveResult p1, ClastPlaceExpMaxBot.UtilityMoveResult p2) {
        return Math.abs(p1.value - p2.value);
    }

    private static boolean equals(ClastPlaceExpMaxBot.UtilityMoveResult p1, ClastPlaceExpMaxBot.UtilityMoveResult p2) {
        return p1.value == p2.value;
    }

    private static ClastPlaceExpMaxBot.UtilityMoveResult randomCentroid() {
        Random r = new Random();
        double randomValue = - 1 + (1 - -1) * r.nextDouble();
        return new ClastPlaceExpMaxBot.UtilityMoveResult(randomValue, null);
    }

    public static class Cluster {
        public ClastPlaceExpMaxBot.UtilityMoveResult centroid;
        public List<ClastPlaceExpMaxBot.UtilityMoveResult> points;

        public Cluster(ClastPlaceExpMaxBot.UtilityMoveResult centroid) {
            this.centroid = centroid;
            this.points = new ArrayList<>();
        }
    }



}