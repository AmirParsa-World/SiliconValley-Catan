package model;

import java.io.Serializable;
import java.util.ArrayList;
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
        this(5, 5); // basic map game.
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
        int activeSectors = totalSectors - 1; // یکی از خانه‌ها همیشه برای رگولاتور (بازرس) است

        List<ResourceType> resourcePool = new ArrayList<>();

        // ۵ منبع فعال بازی
        ResourceType[] activeTypes = {
                ResourceType.TALENT, ResourceType.CAPITAL,
                ResourceType.CLOUD, ResourceType.PATENT, ResourceType.DATA
        };

        // ⚖️ محاسبه عادلانه سهم هر منبع بر اساس ابعاد نقشه
        int baseCount = activeSectors / activeTypes.length; // سهم مساوی پایه
        int remainder = activeSectors % activeTypes.length; // باقی‌مانده تقسیم برای توزیع رندوم

        // ۱. اضافه کردن منابع پایه به استخر
        for (ResourceType type : activeTypes) {
            for (int i = 0; i < baseCount; i++) {
                resourcePool.add(type);
            }
        }

        // ۲. توزیع کاملاً عادلانه باقی‌مانده‌ها (به هر کدام حداکثر ۱ کارت اضافه می‌شود تا بالانس به هم نخورد)
        List<ResourceType> shuffleTypes = new ArrayList<>(List.of(activeTypes));
        Collections.shuffle(shuffleTypes);
        for (int i = 0; i < remainder; i++) {
            resourcePool.add(shuffleTypes.get(i));
        }
        Collections.shuffle(resourcePool); // مخلوط کردن نهایی منابع


        // 🎲 ۳. تولید داینامیک اعداد تاس بر اساس اولویت احتمالات ریاضی (بدون عدد ۷)
        List<Integer> numberPool = new ArrayList<>();
        // ترتیب اولویت بر اساس احتمال بالای تاس (ابتدا بهترین خانه‌ها مثل ۶ و ۸، سپس معمولی‌ها و در آخر ۲ و ۱۲)
        int[] priorityNumbers = {6, 8, 5, 9, 4, 10, 3, 11, 2, 12};

        for (int i = 0; i < activeSectors; i++) {
            // استفاده از عملگر % برای چرخشِ عادلانه روی آرایه اولویت‌ها در هر ابعادی
            numberPool.add(priorityNumbers[i % priorityNumbers.length]);
        }
        Collections.shuffle(numberPool); // مخلوط کردن نهایی اعداد تاس برای تصادفی شدن نقشه

        // فرستادن اطلاعات برای چیدمان نهایی روی ماتریکس نقشه
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