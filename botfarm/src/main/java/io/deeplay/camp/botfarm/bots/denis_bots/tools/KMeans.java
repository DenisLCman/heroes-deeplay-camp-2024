package io.deeplay.camp.botfarm.bots.denis_bots.tools;

import io.deeplay.camp.botfarm.bots.denis_bots.entities.UtilityMoveResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Класс, реализующий алгоритм Кластеризации
 * KMeans++
 */
public class KMeans {

    private final Random random = new Random();

    public List<Cluster> kMeansCluster(List<UtilityMoveResult> points, int k, int maxIter) {
        List<Cluster> clusters = initializeClusters(points, k);
        for (int iter = 0; iter < maxIter; iter++) {
            for (Cluster cluster : clusters) {
                cluster.points.clear();
            }
            for (UtilityMoveResult point : points) {
                Cluster closestCluster = findCloseCluster(clusters, point);
                closestCluster.points.add(point);
            }
            boolean centroidsChanged = false;
            for (Cluster cluster : clusters) {
                UtilityMoveResult newCentroid = calcCentroid(cluster.points);
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

    private Cluster findCloseCluster(List<Cluster> clusters, UtilityMoveResult point) {
        Cluster closeCluster = null;
        double minDistance = Double.MAX_VALUE;
        for (Cluster cluster : clusters) {
            double distance = calcDistance(cluster.centroid, point);
            if (distance < minDistance) {
                minDistance = distance;
                closeCluster = cluster;
            }
        }
        return closeCluster;
    }

    private UtilityMoveResult calcCentroid(List<UtilityMoveResult> points) {
        if (points.isEmpty()) {
            return randCentroid();
        }
        double sum = 0;
        for (UtilityMoveResult point : points) {
            sum += point.getValue();
        }
        double average = sum / points.size();

        return new UtilityMoveResult(average, null);
    }

    private double calcDistance(UtilityMoveResult p1, UtilityMoveResult p2) {
        return Math.abs(p1.getValue() - p2.getValue());
    }

    private boolean equals(UtilityMoveResult p1, UtilityMoveResult p2) {
        return p1.getValue() == p2.getValue();
    }

    private UtilityMoveResult randCentroid() {
        Random r = new Random();
        double min = -1;
        double max = 1;
        double randomValue = min + (max - min) * r.nextDouble();
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