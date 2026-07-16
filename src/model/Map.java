package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Map implements Serializable {
    private static final int SECTOR_ROWS = 5;
    private static final int SECTOR_COLS = 5;
    private static final int VERTEX_ROWS = 6;
    private static final int VERTEX_COLS = 6;

    private final Sector[][] sectors;
    private final Vertex[][] vertices;
    private Regulator auditor;

    private static final long serialVersionUID = 1L;

    public Map() {
        this.sectors = new Sector[SECTOR_ROWS][SECTOR_COLS];
        this.vertices = new Vertex[VERTEX_ROWS][VERTEX_COLS];

        initializeVertices();
        spawnAuditorAtRandom();
        generateRandomSectors(); // Dynamic 4-6 allocator called here!
        initializeEdges();
        wireSectorsToVertices();
    }

    private void initializeVertices() {
        for (int r = 0; r < VERTEX_ROWS; r++) {
            for (int c = 0; c < VERTEX_COLS; c++) {
                this.vertices[r][c] = new Vertex();
            }
        }
    }

    private void spawnAuditorAtRandom() {
        Random random = new Random();
        int randomRow = random.nextInt(SECTOR_ROWS);
        int randomCol = random.nextInt(SECTOR_COLS);
        this.auditor = new Regulator(randomRow, randomCol);
    }

    private void generateRandomSectors() {
        List<ResourceType> resourcePool = new ArrayList<>();

        // Base Allocation: Put exactly 4 of each active resource into the pool (4 * 5 = 20)
        ResourceType[] activeTypes = {
                ResourceType.TALENT, ResourceType.CAPITAL,
                ResourceType.CLOUD, ResourceType.PATENT, ResourceType.DATA
        };
        for (ResourceType type : activeTypes) {
            for (int i = 0; i < 4; i++) {
                resourcePool.add(type);
            }
        }

        // Dynamic Balance Rule: remaining 4 slots randomized up to max of 6
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            ResourceType luckyType = activeTypes[random.nextInt(activeTypes.length)];
            while (Collections.frequency(resourcePool, luckyType) >= 6) {
                luckyType = activeTypes[random.nextInt(activeTypes.length)];
            }
            resourcePool.add(luckyType);
        }
        Collections.shuffle(resourcePool);

        // Populate activation numbers (24 slots, excluding 7)
        List<Integer> numberPool = new ArrayList<>();
        int[] baseNumbers = {2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 5, 6, 8, 9};
        for (int num : baseNumbers) {
            numberPool.add(num);
        }
        Collections.shuffle(numberPool);

        fillSectorMatrix(resourcePool, numberPool);
    }

    private void fillSectorMatrix(List<ResourceType> resourcePool, List<Integer> numberPool) {
        int regulatoryRow = this.auditor.getRow();
        int regulatoryCol = this.auditor.getCol();

        for (int r = 0; r < SECTOR_ROWS; r++) {
            for (int c = 0; c < SECTOR_COLS; c++) {
                if (r == regulatoryRow && c == regulatoryCol) {
                    this.sectors[r][c] = new Sector(ResourceType.REGULATORY, 0);
                } else {
                    ResourceType type = resourcePool.remove(0);
                    int activationNum = numberPool.remove(0);
                    this.sectors[r][c] = new Sector(type, activationNum);
                }
            }
        }
    }

    private void initializeEdges() {
        for (int r = 0; r < VERTEX_ROWS; r++) {
            for (int c = 0; c < VERTEX_COLS; c++) {
                Vertex currentVertex = this.vertices[r][c];

                if (c < VERTEX_COLS - 1) {
                    Vertex rightNeighbor = this.vertices[r][c + 1];
                    Edge horizontalEdge = new Edge(currentVertex, rightNeighbor);
                    currentVertex.addNeighboringEdge(horizontalEdge);
                    rightNeighbor.addNeighboringEdge(horizontalEdge);
                }

                if (r < VERTEX_ROWS - 1) {
                    Vertex topNeighbor = this.vertices[r + 1][c];
                    Edge verticalEdge = new Edge(currentVertex, topNeighbor);
                    currentVertex.addNeighboringEdge(verticalEdge);
                    topNeighbor.addNeighboringEdge(verticalEdge);
                }
            }
        }
    }

    private void wireSectorsToVertices() {
        for (int r = 0; r < SECTOR_ROWS; r++) {
            for (int c = 0; c < SECTOR_COLS; c++) {
                Sector sector = this.sectors[r][c];

                Vertex bottomBl = this.vertices[r][c];
                Vertex bottomBr = this.vertices[r][c + 1];
                Vertex topTl = this.vertices[r + 1][c];
                Vertex topTr = this.vertices[r + 1][c + 1];

                sector.setCorners(bottomBl, bottomBr, topTl, topTr);
            }
        }
    }

    public Sector[][] getSectors() { return this.sectors; }
    public Vertex[][] getVertices() { return this.vertices; }
    public Regulator getAuditor() { return this.auditor; }
}