package controller;

import model.*;
import exception.NotEnoughResourceException;
import exception.StructurePlacementException;
import exception.AlreadyRolledException;

import java.util.ArrayList;
import java.util.List;

public class GameEngine {
    private final List<Player> players;
    private final Market market;
    private int currentPlayerIndex;
    private int currentRound;
    private boolean hasRolledThisTurn;

    // 🏗️ Constructor
    public GameEngine(List<Player> players, Market market) {
        this.players = players != null ? players : new ArrayList<>();
        this.market = market;
        this.currentPlayerIndex = 0;
        this.currentRound = 1;
        this.hasRolledThisTurn = false;
    }

    // 🎲 تاس ریختن قانونمند (جلوگیری از تاس ریختن مجدد در یک نوبت)
    public int rollDice(Dice dice) {
        if (hasRolledThisTurn) {
            throw new AlreadyRolledException("Dear " + getCurrentPlayer().getName() + ", you have already rolled the dice in this turn!");
        }
        int rollResult = dice.roll();
        hasRolledThisTurn = true;
        return rollResult;
    }

    // 🤝 Decentralized Peer-to-Peer Free Trade Processing Contract
    public void executePeerTrade(Player sender, Player receiver,
                                 ResourceType offeredItem, int offeredAmount,
                                 ResourceType requestedItem, int requestedAmount) {

        if (!sender.equals(getCurrentPlayer()) && !receiver.equals(getCurrentPlayer())) {
            throw new IllegalStateException("Trading can only be initiated by or with the active player!");
        }

        if (sender.getResource(offeredItem) < offeredAmount) {
            throw new NotEnoughResourceException(sender.getName() + " lacks the resources to back this offer!");
        }
        if (receiver.getResource(requestedItem) < requestedAmount) {
            throw new NotEnoughResourceException(receiver.getName() + " cannot afford to accept this trade!");
        }

        sender.spendResource(offeredItem, offeredAmount);
        receiver.addResource(offeredItem, offeredAmount);

        receiver.spendResource(requestedItem, requestedAmount);
        sender.addResource(requestedItem, requestedAmount);

        System.out.println("🔄 TRANSACTION SUCCESS: " + sender.getName() + " traded with " + receiver.getName());
    }

    public Player getCurrentPlayer() {
        if (players.isEmpty()) throw new IllegalStateException("No players initialized in the Tech Park!");
        return players.get(currentPlayerIndex);
    }

    public void setHasRolledThisTurn(boolean rolled) {
        this.hasRolledThisTurn = rolled;
    }

    public boolean hasRolledThisTurn() {
        return hasRolledThisTurn;
    }

    // 🔄 Manages turn rotation and triggers the 3-round stagnation tick on the Market
    public void nextTurn() {
        if (players.isEmpty()) return;

        Player activePlayer = getCurrentPlayer();
        if (activePlayer.countPlayerPoint() >= 10) {
            System.out.println("🏆 VICTORY! " + activePlayer.getName() + " reached 10 points and won the game!");
            return;
        }

        if (currentPlayerIndex == players.size() - 1) {
            market.incrementRoundTick();
            currentRound++;
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        hasRolledThisTurn = false;

        System.out.println("🏁 Turn passed. It is now " + getCurrentPlayer().getName() + "'s turn!");
    }

    // 🚨 Handles resource checks when a 7 is rolled
    public void triggerRegulatoryCrisis() {
        System.out.println("🚨 REGULATORY CRISIS TRIPPED!");

        for (Player player : players) {
            int holdingLimit = (player.getRole() == FounderRole.VC_FUNDED) ? 9 : 7;
            int totalCards = player.getTotalResources();

            if (totalCards > holdingLimit) {
                int cardsToDiscard = totalCards / 2;

                System.out.println("💸 TAXED: " + player.getName() + " must discard " + cardsToDiscard + " cards!");

                // کسر خودکار کارت‌ها برای سناریوی تست و بک‌اند بدون گرافیک
                player.discardRandomResources(cardsToDiscard);
                System.out.println("📉 [Backend Auto-Discard] " + player.getName() + "'s hand reduced after penalty.");

                promptPlayerForDiscard(player, cardsToDiscard);
            }
        }
        enableBlockerMovementPhase();
    }

    // 🏗️ بررسی قانون فاصله ۲ یال برای ساخت سازه جدید
    public boolean isValidStructurePlacement(Vertex targetVertex) {
        // ۱. بررسی اینکه آیا خود ورتکس از قبل سازه دارد یا خیر
        if (targetVertex.hasStructure()) {
            return false;
        }
        // ۲. بررسی اینکه آیا همسایه‌های مستقیم (۱ قدم فاصله) سازه دارند یا خیر
        for (Vertex neighbor : targetVertex.getNeighbors()) {
            if (neighbor.hasStructure()) {
                return false;
            }
        }
        return true;
    }


    private void promptPlayerForDiscard(Player player, int cardsToDiscard) {
        System.out.println("[UI Stub] Waiting for " + player.getName() + " to drop " + cardsToDiscard + " cards.");
    }

    private void enableBlockerMovementPhase() {
        System.out.println("[UI Stub] Regulatory Auditor Blocker is ready to be moved on the map.");
    }

    // 💎 سیستم توزیع منابع بر اساس عدد تاس
    public void distributeResources(int rollValue, Map gameMap) {
        if (rollValue == 7) {
            triggerRegulatoryCrisis();
            return;
        }

        System.out.println("🎲 Dice rolled: " + rollValue + ". Distributing resources...");

        for (Sector[] row : gameMap.getSectors()) {
            for (Sector sector : row) {
                if (sector != null && sector.getActivationNumber() == rollValue && !sector.isBlocked()) {
                    ResourceType resource = sector.getResourceType();

                    Vertex[] corners = {
                            sector.getBottomLeft(),
                            sector.getBottomRight(),
                            sector.getTopLeft(),
                            sector.getTopRight()
                    };

                    for (Vertex vertex : corners) {
                        if (vertex != null && vertex.hasStructure() && vertex.getOwner() != null) {
                            Player owner = vertex.getOwner();
                            int yield = (vertex.getStructure().getPoint() == 2) ? 2 : 1;

                            owner.addResource(resource, yield);
                            System.out.println("💎 YIELD: Player " + owner.getName() + " received " + yield + " " + resource + " from sector (" + resource + " - " + rollValue + ")");
                        }
                    }
                }
            }
        }
    }


    // 🕸️ محاسبه طول بلندترین شبکه جاده‌های متصل برای یک بازیکن (الگوریتم DFS)
    public int calculateLongestNetwork(Player player, Map gameMap) {
        int maxLength = 0;
        List<Edge> playerEdges = new ArrayList<>();

        // پیدا کردن تمام یال‌هایی که متعلق به این بازیکن هستند
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                Vertex v = gameMap.getVertices()[r][c];
                for (Edge edge : v.getNeighboringEdges()) {
                    if (edge.getOwner() != null && edge.getOwner().equals(player) && !playerEdges.contains(edge)) {
                        playerEdges.add(edge);
                    }
                }
            }
        }

        // اجرای DFS از هر یال برای پیدا کردن طولانی‌ترین مسیر بدون تکرار یال
        for (Edge startEdge : playerEdges) {
            List<Edge> visited = new ArrayList<>();
            maxLength = Math.max(maxLength, dfsLongestPath(startEdge, player, visited, playerEdges));
        }
        return maxLength;
    }

    private int dfsLongestPath(Edge current, Player player, List<Edge> visited, List<Edge> allPlayerEdges) {
        visited.add(current);
        int maxDepth = 0;

        // بررسی گره‌های دو سر یال فعلی برای ادامه دادن مسیر جاده‌ها
        Vertex[] vertices = {current.getU(), current.getV()};
        for (Vertex v : vertices) {
            for (Edge nextEdge : v.getNeighboringEdges()) {
                if (allPlayerEdges.contains(nextEdge) && !visited.contains(nextEdge)) {
                    // شبیه‌سازی کپی مسیر برای شاخه‌های متفرقه
                    List<Edge> branchVisited = new ArrayList<>(visited);
                    maxDepth = Math.max(maxDepth, dfsLongestPath(nextEdge, player, branchVisited, allPlayerEdges));
                }
            }
        }
        return 1 + maxDepth;
    }

    // 🏆 متد تخصصی تخصیص پویای امتیاز بلندترین زنجیره جاده (Longest Network Award)
    public void updateLongestNetworkAward(Map gameMap) {
        Player currentWinner = null;
        int maxLen = 2; // حداقل طول زنجیره طبق داک باید ۳ باشد (بزرگتر از ۲)

        for (Player p : players) {
            int len = calculateLongestNetwork(p, gameMap);
            if (len > maxLen) {
                maxLen = len;
                currentWinner = p;
            }
        }

        // چاپ خروجی برای صحت‌سنجی در تست‌ها
        if (currentWinner != null) {
            System.out.println("👑 Longest Network belongs to " + currentWinner.getName() + " with length " + maxLen);
        }
    }

    // 🏢 ۱. ساخت محصول اولیه (MVP) با فرمول کامل هزینه‌های داک
    public void buildMVP(Player player, Vertex targetVertex) {
        // هزینه MVP: ۱ سرمایه، ۱ استعداد، ۱ کلاود، ۱ دیتا
        if (player.getResource(ResourceType.CAPITAL) < 1 || player.getResource(ResourceType.TALENT) < 1 ||
                player.getResource(ResourceType.CLOUD) < 1 || player.getResource(ResourceType.DATA) < 1) {
            throw new NotEnoughResourceException(player.getName() + " lacks resources for MVP! (Needs: 1 Capital, 1 Talent, 1 Cloud, 1 Data)");
        }

        if (!isValidStructurePlacement(targetVertex)) {
            throw new exception.InvalidPlacementException("Violation of Distance Rule! Too close to another structure.");
        }

        // کسر هزینه
        player.spendResource(ResourceType.CAPITAL, 1);
        player.spendResource(ResourceType.TALENT, 1);
        player.spendResource(ResourceType.CLOUD, 1);
        player.spendResource(ResourceType.DATA, 1);

        MVP newMvp = new MVP(player, targetVertex);
        targetVertex.setOwner(player);
        targetVertex.setStructure(newMvp);
        player.addStructure(newMvp);

        System.out.println("🏢 SUCCESS: " + player.getName() + " built an MVP on Vertex!");
    }

    // 🦄 ۲. ارتقای MVP به Unicorn با اعمال تخفیف نقش CTO
    public void upgradeToUnicorn(Player player, Vertex targetVertex) {
        if (!targetVertex.hasStructure() || !(targetVertex.getStructure() instanceof MVP) || !targetVertex.getOwner().equals(player)) {
            throw new exception.InvalidPlacementException("You can only upgrade your own MVP!");
        }

        // اعمال قانون تخفیف نقش CTO: ۱ کلاود کمتر نیاز دارد
        int cloudCost = (player.getRole() == FounderRole.GURU_CTO) ? 1 : 2;
        int dataCost = 3;

        if (player.getResource(ResourceType.DATA) < dataCost || player.getResource(ResourceType.CLOUD) < cloudCost) {
            throw new NotEnoughResourceException(player.getName() + " lacks resources for Unicorn! (Needs: 3 Data, " + cloudCost + " Cloud)");
        }

        // کser منابع
        player.spendResource(ResourceType.DATA, dataCost);
        player.spendResource(ResourceType.CLOUD, cloudCost);

        // حذف MVP قدیمی و جایگزینی با Unicorn
        player.getStructures().remove(targetVertex.getStructure());
        Unicorn unicorn = new Unicorn(player, targetVertex);
        targetVertex.setStructure(unicorn);
        player.addStructure(unicorn);

        System.out.println("🦄 SUCCESS: " + player.getName() + " upgraded an MVP to Unicorn!");
    }

    // 🤝 ۳. ساخت قرارداد همکاری (Partnership) روی یال با قانون اتصال
    public void buildPartnership(Player player, Edge targetEdge) {
        if (targetEdge.getOwner() != null) {
            throw new exception.InvalidPlacementException("This edge is already claimed!");
        }

        if (player.getResource(ResourceType.CAPITAL) < 1 || player.getResource(ResourceType.PATENT) < 1) {
            throw new NotEnoughResourceException(player.getName() + " lacks resources for Partnership! (Needs: 1 Capital, 1 Patent)");
        }

        // قانون اتصال: بررسی اینکه آیا یال به جاده یا ساختمانی از همین بازیکن وصل است یا خیر
        boolean isConnected = false;
        Vertex u = targetEdge.getU();
        Vertex v = targetEdge.getV();

        // چک کردن گره u و یال‌های متصل به آن
        if (u.getOwner() != null && u.getOwner().equals(player)) isConnected = true;
        for (Edge e : u.getNeighboringEdges()) {
            if (e.getOwner() != null && e.getOwner().equals(player)) isConnected = true;
        }

        // چک کردن گره v و یال‌های متصل به آن
        if (v.getOwner() != null && v.getOwner().equals(player)) isConnected = true;
        for (Edge e : v.getNeighboringEdges()) {
            if (e.getOwner() != null && e.getOwner().equals(player)) isConnected = true;
        }

        if (!isConnected) {
            throw new exception.InvalidPlacementException("Partnership must connect to your existing network!");
        }

        player.spendResource(ResourceType.CAPITAL, 1);
        player.spendResource(ResourceType.PATENT, 1);

        targetEdge.setOwner(player);
        targetEdge.setPartnership(true);
        System.out.println("🤝 SUCCESS: " + player.getName() + " established a Partnership!");
    }

    // 🚨 ۴. بررسی محدودیت جابجایی مهره بازرس (Auditor) طبق بند ۲ بخش ۷ داک
    public void moveAuditor(Player rollingPlayer, int targetRow, int targetCol, Map gameMap) {
        Sector targetSector = gameMap.getSectors()[targetRow][targetCol];

        // آیا کلاً سکتور صاحب‌داری روی کل مپ وجود دارد؟
        boolean anySectorHasPlayerCompany = false;
        for (Sector[] row : gameMap.getSectors()) {
            for (Sector sec : row) {
                if (sec != null && hasPlayersOnSector(sec)) {
                    anySectorHasPlayerCompany = true;
                    break;
                }
            }
        }

        // اگر سکتورهای صاحب‌دار وجود دارند، بازرس حتماً باید روی یکی از آن‌ها برود
        if (anySectorHasPlayerCompany && !hasPlayersOnSector(targetSector)) {
            throw new exception.InvalidAuditorPlacementException("You must place the Auditor on a sector that has at least one player's structure!");
        }

        // برداشتن بازرس قدیمی و جابجایی آن
        for (Sector[] row : gameMap.getSectors()) {
            for (Sector sec : row) {
                if (sec != null && sec.isBlocked()) {
                    sec.unblock();
                }
            }
        }

        targetSector.block();
        gameMap.getAuditor().move(targetRow, targetCol);
        System.out.println("🚨 AUDITOR MOVED: Placed on sector (" + targetRow + "," + targetCol + ")");
    }

    private boolean hasPlayersOnSector(Sector sector) {
        Vertex[] corners = {sector.getBottomLeft(), sector.getBottomRight(), sector.getTopLeft(), sector.getTopRight()};
        for (Vertex v : corners) {
            if (v != null && v.hasStructure()) return true;
        }
        return false;
    }

    // 🚀 ۵. فاز راه‌اندازی شروع بازی (Setup Phase) برای قرار دادن سازه‌های اولیه رایگان
    public void setupPlaceMVPAndPartnership(Player player, Vertex vertex, Edge edge) {
        // بدون کسر منابع و فاقد قانون اتصال جاده‌ها برای فاز استقرار اولیه مجانی
        if (!isValidStructurePlacement(vertex)) {
            throw new exception.InvalidPlacementException("Distance rule violated during setup phase!");
        }

        MVP setupMvp = new MVP(player, vertex);
        vertex.setOwner(player);
        vertex.setStructure(setupMvp);
        player.addStructure(setupMvp);

        edge.setOwner(player);
        edge.setPartnership(true);

        System.out.println("📦 SETUP SUCCESS: " + player.getName() + " placed starting MVP & Partnership!");
    }

}