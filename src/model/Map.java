package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Map implements Serializable {
    private final int sectorRows;
    private final int sectorCols;
    private final int vertexRows;
    private final int vertexCols;

    private final Sector[][] sectors;
    private final Vertex[][] vertices;
    private Regulator auditor;

    private static final long serialVersionUID = 1L;

    public Map() {
        this(5, 5); // default grid size
    }

    public Map(int sectorRows, int sectorCols) {
        this.sectorRows = sectorRows;
        this.sectorCols = sectorCols;
        this.vertexRows = sectorRows + 1;
        this.vertexCols = sectorCols + 1;

        this.sectors = new Sector[this.sectorRows][this.sectorCols];
        this.vertices = new Vertex[this.vertexRows][this.vertexCols];

        initializeVertices();
        spawnAuditorAtRandom();
        generateRandomSectors();
        initializeEdges();
        wireSectorsToVertices();
    }

    private void initializeVertices() {
        for (int r = 0; r < vertexRows; r++) {
            for (int c = 0; c < vertexCols; c++) {
                this.vertices[r][c] = new Vertex();
            }
        }
    }

    private void spawnAuditorAtRandom() {
        Random random = new Random();
        int randomRow = random.nextInt(sectorRows);
        int randomCol = random.nextInt(sectorCols);
        this.auditor = new Regulator(randomRow, randomCol);
    }

    private void generateRandomSectors() {
        int totalSectors = sectorRows * sectorCols;
        int activeSectors = totalSectors - 1; // 1 sector is always reserved for the tax auditor

        List<ResourceType> resourcePool = new ArrayList<>();

        ResourceType[] activeTypes = {
                ResourceType.TALENT, ResourceType.CAPITAL,
                ResourceType.CLOUD, ResourceType.PATENT, ResourceType.DATA
        };

        // Balance resources dynamically based on map size
        int baseCount = activeSectors / activeTypes.length;
        int remainder = activeSectors % activeTypes.length;

        for (ResourceType type : activeTypes) {
            for (int i = 0; i < baseCount; i++) {
                resourcePool.add(type);
            }
        }

        // Evenly spread out the leftover slots so things stay balanced
        List<ResourceType> shuffleTypes = new ArrayList<>(Arrays.asList(activeTypes));
        Collections.shuffle(shuffleTypes);
        for (int i = 0; i < remainder; i++) {
            resourcePool.add(shuffleTypes.get(i));
        }
        Collections.shuffle(resourcePool);

        // Weight token generation using standard Catan odds (excluding 7)
        List<Integer> numberPool = new ArrayList<>();
        int[] priorityNumbers = {6, 8, 5, 9, 4, 10, 3, 11, 2, 12};

        for (int i = 0; i < activeSectors; i++) {
            // Safe wrap-around using modulo for any map scale
            numberPool.add(priorityNumbers[i % priorityNumbers.length]);
        }
        Collections.shuffle(numberPool);

        fillSectorMatrix(resourcePool, numberPool);
    }

    private void fillSectorMatrix(List<ResourceType> resourcePool, List<Integer> numberPool) {
        int regulatoryRow = this.auditor.getRow();
        int regulatoryCol = this.auditor.getCol();

        for (int r = 0; r < sectorRows; r++) {
            for (int c = 0; c < sectorCols; c++) {
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
        for (int r = 0; r < vertexRows; r++) {
            for (int c = 0; c < vertexCols; c++) {
                Vertex currentVertex = this.vertices[r][c];

                if (c < vertexCols - 1) {
                    Vertex rightNeighbor = this.vertices[r][c + 1];
                    Edge horizontalEdge = new Edge(currentVertex, rightNeighbor);
                    currentVertex.addNeighboringEdge(horizontalEdge);
                    rightNeighbor.addNeighboringEdge(horizontalEdge);
                }

                if (r < vertexRows - 1) {
                    Vertex topNeighbor = this.vertices[r + 1][c];
                    Edge verticalEdge = new Edge(currentVertex, topNeighbor);
                    currentVertex.addNeighboringEdge(verticalEdge);
                    topNeighbor.addNeighboringEdge(verticalEdge);
                }
            }
        }
    }

    private void wireSectorsToVertices() {
        for (int r = 0; r < sectorRows; r++) {
            for (int c = 0; c < sectorCols; c++) {
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