package io.deeplay.camp.botfarm.bots.denis_bots;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KMeans {

    private final Random random = new Random();

    public List<Cluster> kMeansCluster(List<UtilityMoveResult> points, int k, int maxIterations) {
        List<Cluster> clusters = initializeClusters(points, k);

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            for (Cluster cluster : clusters) {
                cluster.points.clear();
            }
            for (UtilityMoveResult point : points) {
                Cluster closestCluster = findClosestCluster(clusters, point);
                closestCluster.points.add(point);
            }

            boolean centroidsChanged = false;
            for (Cluster cluster : clusters) {
                UtilityMoveResult newCentroid = calculateCentroid(cluster.points);
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

    private List<Cluster> initializeClusters(List<UtilityMoveResult> points, int k) {
        List<Cluster> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            UtilityMoveResult centroid = points.get(random.nextInt(points.size()));
            clusters.add(new Cluster(centroid));
        }
        return clusters;
    }

    private Cluster findClosestCluster(List<Cluster> clusters, UtilityMoveResult point) {
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

    private UtilityMoveResult calculateCentroid(List<UtilityMoveResult> points) {
        if (points.isEmpty()) {
            return randomCentroid();
        }

        double sum = 0;
        for (UtilityMoveResult point : points) {
            sum += point.value;
        }
        double average = sum / points.size();

        return new UtilityMoveResult(average, null); // null для MakeMoveEvent, так как мы считаем только значение
    }

    private double calculateDistance(UtilityMoveResult p1, UtilityMoveResult p2) {
        return Math.abs(p1.value - p2.value);
    }

    private boolean equals(UtilityMoveResult p1, UtilityMoveResult p2) {
        return p1.value == p2.value;
    }

    private UtilityMoveResult randomCentroid() {
        Random r = new Random();
        double randomValue = - 1 + (1 - -1) * r.nextDouble();
        return new UtilityMoveResult(randomValue, null);
    }

    public class Cluster {
        public UtilityMoveResult centroid;
        public List<UtilityMoveResult> points;

        public Cluster(UtilityMoveResult centroid) {
            this.centroid = centroid;
            this.points = new ArrayList<>();
        }
    }



}