package controller;

import model.*;
import exception.NotEnoughResourceException;
import exception.AlreadyRolledException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameEngine implements Serializable {
    private final List<Player> players;
    private final Market market;
    private int currentPlayerIndex;
    private int currentRound;
    private boolean hasRolledThisTurn;
    private final Map gameMap; // 👈 نقشه ثابت بازی

    // 🔄 فیلدهای مدیریت فاز رفت و برگشت (Snake Draft)
    private GamePhase currentPhase;
    private final List<Integer> setupOrder = new ArrayList<>();
    private int setupStep = 0;

    // needed for save and load the game.
    private static final long serialVersionUID = 1L;

    // needed for giving a log about gameFlow
    private final List<String> gameLog = new ArrayList<>();

    // 🏗️ Constructor (مپ را به عنوان فیلد اصلی بازی تحویل می‌گیرد)
    public GameEngine(List<Player> players, Market market, Map gameMap) {
        this.players = players != null ? players : new ArrayList<>();
        this.market = market;
        this.gameMap = gameMap;
        this.currentPlayerIndex = 0;
        this.currentRound = 1;
        this.hasRolledThisTurn = false;
        this.currentPhase = GamePhase.SETUP; // بازی همیشه با فاز ست‌آپ شروع می‌شود

        initializeSetupOrder();
    }

    /**
     * تولید لیست نوبت‌های فاز اول به صورت Snake Draft (رفت و برگشت)
     * برای ۲ بازیکن: [0, 1, 1, 0]
     * برای ۳ بازیکن: [0, 1, 2, 2, 1, 0]
     */
    private void initializeSetupOrder() {
        setupOrder.clear();
        int numberOfPlayers = players.size();
        if (numberOfPlayers == 0) return;

        // مسیر رفت
        for (int i = 0; i < numberOfPlayers; i++) {
            setupOrder.add(i);
        }
        // مسیر برگشت
        for (int i = numberOfPlayers - 1; i >= 0; i--) {
            setupOrder.add(i);
        }

        this.setupStep = 0;
        this.currentPlayerIndex = setupOrder.get(0);
    }

    public Map getGameMap() {
        return this.gameMap;
    }

    public Market getMarket() {
        return this.market;
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    // 🎲 تاس ریختن قانونمند
    public int rollDice(Dice dice) {
        if (hasRolledThisTurn) {
            throw new AlreadyRolledException("Dear " + getCurrentPlayer().getName() + ", you have already rolled the dice in this turn!");
        }
        int rollResult = dice.roll();
        hasRolledThisTurn = true;

        // 📝 ثبت لاگ تاس
        log(getCurrentPlayer().getName() + " rolled a " + rollResult + " 🎲");

        return rollResult;
    }

    // 🤝 تجارت آزاد بازیکن با بازیکن
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

        // ثبت لاگ معامله
        log("🔄 TRADE: " + sender.getName() + " traded with " + receiver.getName() +
                " (Offered: " + offeredAmount + " " + offeredItem + " | Requested: " + requestedAmount + " " + requestedItem + ")");
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

    public GamePhase getCurrentPhase() {
        return this.currentPhase;
    }

    private boolean checkVictoryCondition() {
        Player activePlayer = getCurrentPlayer();
        if (activePlayer.countPlayerPoint() >= 10) {
            this.currentPhase = GamePhase.FINISHED;
            log("🏆🏆🏆 VICTORY! " + activePlayer.getName() + " reached " + activePlayer.countPlayerPoint() + " points and won the game! 🏆🏆🏆");
            return true;
        }
        return false;
    }

    // 🔄 مدیریت نوبت‌ها
    public void nextTurn() {
        if (players.isEmpty()) return;

        if (currentPhase == GamePhase.SETUP) {
            setupStep++;
            if (setupStep < setupOrder.size()) {
                currentPlayerIndex = setupOrder.get(setupStep);
                log("🔄 Setup Turn: Next draft belongs to " + getCurrentPlayer().getName());
            } else {
                this.currentPhase = GamePhase.NORMAL;
                this.currentPlayerIndex = 0;
                this.hasRolledThisTurn = false;
                log("📢 Setup Phase completed! Transitioning to NORMAL phase.");
                log("🏁 Turn started: Current Player is " + getCurrentPlayer().getName());
            }
        } else if (currentPhase == GamePhase.NORMAL) {

            if (checkVictoryCondition()) {
                return;
            }

            if (currentPlayerIndex == players.size() - 1) {
                market.incrementRoundTick();
                currentRound++;
                log("📅 Round " + currentRound + " has started!");
            }

            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            hasRolledThisTurn = false;

            log("🏁 Turn passed. It is now " + getCurrentPlayer().getName() + "'s turn!");
        }
    }

    public java.util.Map<Player, Integer> triggerRegulatoryCrisis() {
        log("🚨 REGULATORY CRISIS TRIPPED! Tax authorities are auditing players...");

        java.util.Map<Player, Integer> discardMap = new java.util.LinkedHashMap<>();

        for (Player player : players) {
            int holdingLimit = (player.getRole() == FounderRole.VC_FUNDED) ? 9 : 7;
            int totalCards = player.getTotalResources();

            if (totalCards > holdingLimit) {
                int cardsToDiscard = totalCards / 2;
                log("💸 TAXED: " + player.getName() + " had " + totalCards + " cards (limit is " + holdingLimit + ") must discard " + cardsToDiscard + "!");
                discardMap.put(player, cardsToDiscard);
            }
        }

        enableBlockerMovementPhase();
        return discardMap;
    }

    public boolean isValidStructurePlacement(Vertex targetVertex) {
        if (targetVertex.hasStructure()) {
            return false;
        }
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

    public java.util.Map<Player, Integer> distributeResources(int rollValue) {
        if (rollValue == 7) {
            return triggerRegulatoryCrisis();
        }

        log("🎲 Dice rolled: " + rollValue + ". Distributing resources...");
        java.util.Map<Player, Integer> yields = new java.util.HashMap<>();

        // 🛡️ مجموعه کمکی برای ثبت گره‌هایی که در این نوبت کارت تولید کرده‌اند (جلوگیری از واریز دوگانه)
        java.util.Set<Vertex> processedVertices = new java.util.HashSet<>();

        for (Sector[] row : this.gameMap.getSectors()) {
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

                            if (vertex.isLockedByAuditor()) {
                                continue;
                            }

                            // 🎯 گام طلایی: اگر این گره قبلاً در همین نوبت کارت تولید کرده، آن را رد کن!
                            if (processedVertices.contains(vertex)) {
                                continue;
                            }

                            Player owner = vertex.getOwner();
                            int yield = (vertex.getStructure().getPoint() == 2) ? 2 : 1;

                            owner.addResource(resource, yield);
                            log("📦 " + owner.getName() + " earned " + yield + " " + resource + " from Sector " + rollValue);

                            // ثبت آمار برای خروجی نهایی
                            yields.put(owner, yields.getOrDefault(owner, 0) + yield);

                            // 🔒 قفل کردن گره برای باقی‌مانده این دور از محاسبات تاس
                            processedVertices.add(vertex);
                        }
                    }
                }
            }
        }
        return yields; // برگرداندن مپ توزیع به جای null برای صحت لاگ‌های فرانت‌اند
    }

    public int calculateLongestNetwork(Player player) {
        int maxLength = 0;
        List<Edge> playerEdges = new ArrayList<>();
        int vertexRows = this.gameMap.getVertices().length;
        int vertexCols = this.gameMap.getVertices()[0].length;

        for (int r = 0; r < vertexRows; r++) {
            for (int c = 0; c < vertexCols; c++) {
                Vertex v = this.gameMap.getVertices()[r][c];
                for (Edge edge : v.getNeighboringEdges()) {
                    if (edge.getOwner() != null && edge.getOwner().equals(player) && !playerEdges.contains(edge)) {
                        playerEdges.add(edge);
                    }
                }
            }
        }
        for (Edge startEdge : playerEdges) {
            List<Edge> visited = new ArrayList<>();
            maxLength = Math.max(maxLength, dfsLongestPath(startEdge, player, visited, playerEdges));
        }
        return maxLength;
    }

    private int dfsLongestPath(Edge current, Player player, List<Edge> visited, List<Edge> allPlayerEdges) {
        visited.add(current);
        int maxDepth = 0;

        Vertex[] vertices = {current.getU(), current.getV()};
        for (Vertex v : vertices) {
            for (Edge nextEdge : v.getNeighboringEdges()) {
                if (allPlayerEdges.contains(nextEdge) && !visited.contains(nextEdge)) {
                    List<Edge> branchVisited = new ArrayList<>(visited);
                    maxDepth = Math.max(maxDepth, dfsLongestPath(nextEdge, player, branchVisited, allPlayerEdges));
                }
            }
        }
        return 1 + maxDepth;
    }

    public void updateLongestNetworkAward() {
        Player currentWinner = null;
        int maxLen = 2;

        for (Player p : players) {
            int len = calculateLongestNetwork(p);
            if (len > maxLen) {
                maxLen = len;
                currentWinner = p;
            }
        }

        for (Player p : players) {
            p.setHasLongestNetwork(p.equals(currentWinner));
        }

        if (currentWinner != null) {
            log("👑 Longest Network belongs to " + currentWinner.getName() + " with length " + maxLen);
        }
    }

    // 🏢 ساخت محصول اولیه (MVP)
    public void buildMVP(Player player, Vertex targetVertex) {
        if (targetVertex.isLockedByAuditor()) {
            throw new exception.InvalidPlacementException("🚨 REGULATORY BLOCK: The Tax Inspector is auditing this sector! You cannot build here.");
        }

        // 🎯 اصلاح نهایی ارور مینی‌مال چندخطی برای جلوگیری از سه‌نقطه شدن متن
        if (player.getResource(ResourceType.CAPITAL) < 1 || player.getResource(ResourceType.TALENT) < 1 ||
                player.getResource(ResourceType.CLOUD) < 1 || player.getResource(ResourceType.DATA) < 1) {
            throw new NotEnoughResourceException("Missing for MVP:\n1 Capital, 1 Talent\n1 Cloud, 1 Data");
        }

        if (!isValidStructurePlacement(targetVertex)) {
            throw new exception.InvalidPlacementException("Violation of Distance Rule! Too close to another structure.");
        }

        player.spendResource(ResourceType.CAPITAL, 1);
        player.spendResource(ResourceType.TALENT, 1);
        player.spendResource(ResourceType.CLOUD, 1);
        player.spendResource(ResourceType.DATA, 1);

        MVP newMvp = new MVP(player, targetVertex);
        targetVertex.setOwner(player);
        targetVertex.setStructure(newMvp);
        player.addStructure(newMvp);

        log("🏢 SUCCESS: " + player.getName() + " built an MVP on Vertex!");
    }

    // 🦄 ارتقای MVP به Unicorn
    public void upgradeToUnicorn(Player player, Vertex targetVertex) {
        if (targetVertex.isLockedByAuditor()) {
            throw new exception.InvalidPlacementException("🚨 REGULATORY BLOCK: The Tax Inspector is auditing this sector! You cannot build or upgrade here.");
        }

        System.out.println("DEBUG UNICORN: Player=" + player.getName() +
                " | Backend Data=" + player.getResource(ResourceType.DATA) +
                " | Backend Cloud=" + player.getResource(ResourceType.CLOUD));

        if (!targetVertex.hasStructure() || !(targetVertex.getStructure() instanceof MVP) || !targetVertex.getOwner().equals(player)) {
            throw new exception.InvalidPlacementException("You can only upgrade your own MVP!");
        }

        int cloudCost = (player.getRole() == FounderRole.GURU_CTO) ? 1 : 2;
        int dataCost = 3;

        // 🎯 اصلاح نهایی ارور مینی‌مال چندخطی یونی‌کورن
        if (player.getResource(ResourceType.DATA) < dataCost || player.getResource(ResourceType.CLOUD) < cloudCost) {
            throw new NotEnoughResourceException("Missing for Unicorn:\n3 Data, " + cloudCost + " Cloud");
        }

        player.spendResource(ResourceType.DATA, dataCost);
        player.spendResource(ResourceType.CLOUD, cloudCost);

        player.getStructures().remove(targetVertex.getStructure());
        Unicorn unicorn = new Unicorn(player, targetVertex);
        targetVertex.setStructure(unicorn);
        player.addStructure(unicorn);

        log("🦄 SUCCESS: " + player.getName() + " upgraded an MVP to Unicorn!");
    }

    // 🤝 ساخت قرارداد همکاری (Partnership)
    public void buildPartnership(Player player, Edge targetEdge) {
        if (targetEdge.getOwner() != null) {
            throw new exception.InvalidPlacementException("This edge is already claimed!");
        }

        // 🎯 اصلاح نهایی ارور مینی‌مال چندخطی جاده
        if (player.getResource(ResourceType.CAPITAL) < 1 || player.getResource(ResourceType.PATENT) < 1) {
            throw new NotEnoughResourceException("Missing for Road:\n1 Capital, 1 Patent");
        }

        boolean isConnected = false;
        Vertex u = targetEdge.getU();
        Vertex v = targetEdge.getV();

        if (u.getOwner() != null && u.getOwner().equals(player)) isConnected = true;
        for (Edge e : u.getNeighboringEdges()) {
            if (e.getOwner() != null && e.getOwner().equals(player)) isConnected = true;
        }

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
        log("🤝 SUCCESS: " + player.getName() + " established a Partnership!");
    }

    // 🕵️‍♂️ جابجایی بازرس مالیاتی
    public void moveAuditor(Player rollingPlayer, int targetRow, int targetCol) {
        Sector targetSector = this.gameMap.getSectors()[targetRow][targetCol];

        boolean anySectorHasPlayerCompany = false;
        for (Sector[] row : this.gameMap.getSectors()) {
            for (Sector sec : row) {
                if (sec != null && hasPlayersOnSector(sec)) {
                    anySectorHasPlayerCompany = true;
                    break;
                }
            }
        }

        if (anySectorHasPlayerCompany && !hasPlayersOnSector(targetSector)) {
            throw new exception.InvalidAuditorPlacementException("You must place the Auditor on a sector that has at least one player's structure!");
        }

        for (Sector[] row : this.gameMap.getSectors()) {
            for (Sector sec : row) {
                if (sec != null && sec.isBlocked()) {
                    Vertex[] oldCorners = { sec.getBottomLeft(), sec.getBottomRight(), sec.getTopLeft(), sec.getTopRight() };
                    for (Vertex v : oldCorners) {
                        if (v != null) {
                            v.setLockedByAuditor(false);
                        }
                    }
                    sec.unblock();
                }
            }
        }

        targetSector.block();
        Vertex[] newCorners = { targetSector.getBottomLeft(), targetSector.getBottomRight(), targetSector.getTopLeft(), targetSector.getTopRight() };
        for (Vertex v : newCorners) {
            if (v != null) {
                v.setLockedByAuditor(true);
            }
        }

        this.gameMap.getAuditor().move(targetRow, targetCol);
        log("🕵️‍♂️ AUDITOR: Moved to sector (" + targetRow + "," + targetCol + ")");
    }

    private boolean hasPlayersOnSector(Sector sector) {
        Vertex[] corners = {sector.getBottomLeft(), sector.getBottomRight(), sector.getTopLeft(), sector.getTopRight()};
        for (Vertex v : corners) {
            if (v != null && v.hasStructure()) return true;
        }
        return false;
    }

    public void setupPlaceMVPAndPartnership(Player player, Vertex vertex, Edge edge) {
        if (!isValidStructurePlacement(vertex)) {
            throw new exception.InvalidPlacementException("Distance rule violated during setup phase!");
        }

        MVP setupMvp = new MVP(player, vertex);
        vertex.setOwner(player);
        vertex.setStructure(setupMvp);
        player.addStructure(setupMvp);

        edge.setOwner(player);
        edge.setPartnership(true);

        log("📦 SETUP SUCCESS: " + player.getName() + " placed starting MVP & Partnership!");
    }

    public void log(String message) {
        String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        String formattedLog = "[" + timestamp + "] " + message;
        gameLog.add(formattedLog);
        System.out.println(formattedLog);
    }

    public List<String> getGameLog() {
        return new ArrayList<>(this.gameLog);
    }

    // 🤖 هوش مصنوعی فاز ست‌آپ ربات
    public void playBotSetupTurn() {
        Player activePlayer = getCurrentPlayer();
        if (!(activePlayer instanceof SimpleBot)) return;

        SimpleBot bot = (SimpleBot) activePlayer;
        log("🤖 Bot " + bot.getName() + " is placing its starting MVP & Partnership automatically...");

        Vertex targetVertex = findRandomValidVertexForSetup();

        if (targetVertex != null) {
            List<Edge> availableEdges = new ArrayList<>();
            for (Edge edge : targetVertex.getNeighboringEdges()) {
                if (edge != null && edge.getOwner() == null) {
                    availableEdges.add(edge);
                }
            }

            if (!availableEdges.isEmpty()) {
                Edge targetEdge = availableEdges.get(new java.util.Random().nextInt(availableEdges.size()));
                try {
                    setupPlaceMVPAndPartnership(bot, targetVertex, targetEdge);
                    nextTurn();
                } catch (Exception e) {
                    log("⚠️ Bot setup failed: " + e.getMessage());
                }
            } else {
                log("⚠️ Bot " + bot.getName() + " couldn't find an empty adjacent edge during setup!");
            }
        } else {
            log("⚠️ Bot " + bot.getName() + " couldn't find a valid location during setup!");
        }
    }

    // 🔍 متدهای گراف سرچ و مکان‌یابی نقشه (مورد نیاز هوش مصنوعی ربات‌ها)
    private Vertex findRandomValidVertexForSetup() {
        List<Vertex> validVertices = new ArrayList<>();
        for (Vertex[] row : this.gameMap.getVertices()) {
            for (Vertex v : row) {
                if (v != null && isValidStructurePlacement(v)) {
                    validVertices.add(v);
                }
            }
        }
        if (!validVertices.isEmpty()) {
            return validVertices.get(new java.util.Random().nextInt(validVertices.size()));
        }
        return null;
    }

    private Vertex findRandomValidVertexForNormal(SimpleBot bot) {
        List<Vertex> validVertices = new ArrayList<>();
        for (Sector[] row : this.gameMap.getSectors()) {
            for (Sector sector : row) {
                if (sector == null) continue;
                Vertex[] corners = {sector.getBottomLeft(), sector.getBottomRight(), sector.getTopLeft(), sector.getTopRight()};
                for (Vertex v : corners) {
                    if (v != null && isValidStructurePlacement(v)) {
                        for (Edge edge : v.getNeighboringEdges()) {
                            if (edge != null && edge.getOwner() != null && edge.getOwner().equals(bot)) {
                                validVertices.add(v);
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (!validVertices.isEmpty()) {
            return validVertices.get(new java.util.Random().nextInt(validVertices.size()));
        }
        return null;
    }

    private Vertex findBotMvpToUpgrade(SimpleBot bot) {
        for (Sector[] row : this.gameMap.getSectors()) {
            for (Sector sector : row) {
                if (sector == null) continue;
                Vertex[] corners = {sector.getBottomLeft(), sector.getBottomRight(), sector.getTopLeft(), sector.getTopRight()};
                for (Vertex v : corners) {
                    if (v != null && v.getOwner() == bot && v.getStructure() instanceof MVP) {
                        return v;
                    }
                }
            }
        }
        return null;
    }

    private Edge findRandomValidEdgeForBot(SimpleBot bot) {
        List<Edge> validEdges = new ArrayList<>();
        for (Sector[] row : this.gameMap.getSectors()) {
            for (Sector sector : row) {
                if (sector == null) continue;
                Vertex[] corners = {sector.getBottomLeft(), sector.getBottomRight(), sector.getTopLeft(), sector.getTopRight()};
                for (Vertex v : corners) {
                    if (v == null) continue;

                    boolean connectedToBotTerritory = (v.getOwner() == bot);
                    if (!connectedToBotTerritory) {
                        for (Edge e : v.getNeighboringEdges()) {
                            if (e != null && e.getOwner() == bot) {
                                connectedToBotTerritory = true;
                                break;
                            }
                        }
                    }

                    if (connectedToBotTerritory) {
                        for (Edge edge : v.getNeighboringEdges()) {
                            if (edge != null && edge.getOwner() == null) {
                                if (!validEdges.contains(edge)) {
                                    validEdges.add(edge);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!validEdges.isEmpty()) {
            return validEdges.get(new java.util.Random().nextInt(validEdges.size()));
        }
        return null;
    }

    // 🤖🧠 متد نهایی و فوق هوشمند نوبت ربات‌ها (بدون تکرار و ۱۰۰٪ کامپایل موفق)
    public void playBotTurn(Dice dice) {
        Player activePlayer = getCurrentPlayer();
        if (!(activePlayer instanceof SimpleBot)) return;

        SimpleBot bot = (SimpleBot) activePlayer;
        log("🤖 Bot " + bot.getName() + " is analyzing financial assets and market rates...");

        // 🏦 ۱. حلقه ترید متوالی برای تامین منابع ساخت MVP
        ResourceType[] requiredForMvp = {ResourceType.CAPITAL, ResourceType.TALENT, ResourceType.CLOUD, ResourceType.DATA};
        boolean keepTrading = true;
        while (keepTrading) {
            keepTrading = false;
            for (ResourceType needed : requiredForMvp) {
                if (bot.getResource(needed) < 1) {
                    for (ResourceType surplus : ResourceType.values()) {
                        if (surplus != needed && bot.getResource(surplus) >= 3) {
                            int sellPrice = market.getSellPrice(surplus);
                            bot.spendResource(surplus, 1);
                            bot.addResource(ResourceType.CAPITAL, sellPrice);
                            log("🔄 Market Trade: Bot " + bot.getName() + " sold 1 " + surplus + " for " + sellPrice + " Capital.");
                            keepTrading = true;
                            break;
                        }
                    }
                    if (needed != ResourceType.CAPITAL) {
                        int buyPrice = market.getBuyPrice(needed);
                        if (bot.getResource(ResourceType.CAPITAL) >= buyPrice) {
                            bot.spendResource(ResourceType.CAPITAL, buyPrice);
                            bot.addResource(needed, 1);
                            log("🔄 Market Trade: Bot " + bot.getName() + " spent " + buyPrice + " Capital to buy 1 " + needed + ".");
                            keepTrading = true;
                        }
                    }
                }
                if (keepTrading) break;
            }
        }

        // 🦄 ۲. اولویت اول -> تلاش برای ارتقا به یونی‌کورن
        if (bot.getResource(ResourceType.DATA) >= 3 && bot.getResource(ResourceType.CLOUD) >= 2) {
            Vertex mvpVertex = findBotMvpToUpgrade(bot);
            if (mvpVertex != null) {
                try {
                    upgradeToUnicorn(bot, mvpVertex);
                    log("🦄✨ VALUATION SPIKE: Bot " + bot.getName() + " upgraded an MVP to a Tech Unicorn!");
                    nextTurn();
                    return;
                } catch (Exception e) {
                    log("⚠️ Bot failed to upgrade Unicorn: " + e.getMessage());
                }
            }
        }

        // 🏢 ۳. اولویت دوم -> ساخت سازه MVP
        boolean canAffordMVP = bot.getResource(ResourceType.CAPITAL) >= 1 &&
                bot.getResource(ResourceType.TALENT) >= 1 &&
                bot.getResource(ResourceType.CLOUD) >= 1 &&
                bot.getResource(ResourceType.DATA) >= 1;

        if (canAffordMVP) {
            Vertex targetVertex = findRandomValidVertexForNormal(bot);
            if (targetVertex != null) {
                try {
                    buildMVP(bot, targetVertex);
                    nextTurn();
                    return;
                } catch (Exception e) {
                    log("⚠️ Bot failed to build MVP: " + e.getMessage());
                }
            } else {
                log("🤖 Bot " + bot.getName() + " has resources for MVP but no valid vertex. Expanding network...");
                tryToBuildBotPartnership(bot);
            }
        } else {
            // 🛣️ ۴. اولویت سوم -> مدیریت هوشمند جاده‌کشی برای پیشروی رندوم
            Vertex nextPossibleVertex = findRandomValidVertexForNormal(bot);
            if (nextPossibleVertex == null) {
                tryToBuildBotPartnership(bot);
            } else {
                log("🤖 Bot " + bot.getName() + " holds resources to save up for an MVP.");
            }
        }

        nextTurn();
    }

    // 🔍 متد کمکی: مدیریت ترید مارکت اختصاصی جاده و پیشروی رندوم ربات در سراسر مپ
    private void tryToBuildBotPartnership(SimpleBot bot) {
        ResourceType[] requiredForRoad = {ResourceType.CAPITAL, ResourceType.PATENT};
        for (ResourceType needed : requiredForRoad) {
            while (bot.getResource(needed) < 1) {
                boolean traded = false;
                for (ResourceType surplus : ResourceType.values()) {
                    if (surplus != needed && bot.getResource(surplus) >= 3) {
                        int sellPrice = market.getSellPrice(surplus);
                        bot.spendResource(surplus, 1);
                        bot.addResource(ResourceType.CAPITAL, sellPrice);
                        log("🔄 Road Trade: Bot " + bot.getName() + " sold 1 " + surplus + " for " + sellPrice + " Capital.");

                        if (needed == ResourceType.PATENT) {
                            int buyPrice = market.getBuyPrice(ResourceType.PATENT);
                            if (bot.getResource(ResourceType.CAPITAL) >= buyPrice) {
                                bot.spendResource(ResourceType.CAPITAL, buyPrice);
                                bot.addResource(ResourceType.PATENT, 1);
                                log("🔄 Road Trade: Bot " + bot.getName() + " spent " + buyPrice + " Capital to buy 1 PATENT.");
                            }
                        }
                        traded = true;
                        break;
                    }
                }
                if (!traded) break;
            }
        }

        Edge targetEdge = findRandomValidEdgeForBot(bot);
        if (targetEdge != null) {
            try {
                buildPartnership(bot, targetEdge);
                log("🛣️ Bot " + bot.getName() + " established a new Partnership path!");
            } catch (Exception e) {
                log("⚠️ Bot failed to build Partnership: " + e.getMessage());
            }
        }
    }
}