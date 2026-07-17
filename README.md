# Silicon Valley Catan

A board game inspired by Catan, re-themed around the Silicon Valley startup ecosystem. Built with JavaFX.

## How to Run

1. Double-click **`run-game.bat`**
2. Select number of players (2-4)
3. For each player, choose **Human** or **Bot**
4. Play the game!

**Prerequisites:** JDK 17 and JavaFX 21 SDK (already included in the project downloads).

## How to Compile (after code changes)

Double-click **`compile.bat`**

## Project Structure

```
src/
├── model/          # Game data model
│   ├── Map.java          # 5x5 sector grid with vertices and edges
│   ├── Sector.java       # Resource tiles with activation numbers
│   ├── Vertex.java       # Intersections where structures are placed
│   ├── Edge.java         # Connections between vertices (roads)
│   ├── Player.java       # Player state, wallet, structures
│   ├── SimpleBot.java    # AI bot player
│   ├── Structure.java    # Abstract base for buildings
│   ├── MVP.java          # Minimum Viable Product (1 point)
│   ├── Unicorn.java      # Unicorn startup (2 points)
│   ├── Dice.java         # Two-d6 dice with individual die tracking
│   ├── ResourceType.java # DATA, PATENT, CLOUD, CAPITAL, TALENT, REGULATORY
│   ├── FounderRole.java  # HACKER_CEO, GURU_CTO, VC_FUNDED, NONE
│   └── Regulator.java    # Auditor blocker position
├── controller/     # Game logic
│   ├── GameEngine.java   # Core game engine (turns, builds, trades)
│   ├── GamePhase.java    # SETUP, NORMAL, FINISHED
│   └── Market.java       # Dynamic market with price fluctuations
├── view/           # JavaFX GUI
│   ├── MainApp.java      # Application entry point, bot orchestration
│   ├── BoardCanvas.java  # Game board with sectors, vertices, edges
│   ├── DicePane.java     # Canvas-drawn dice with dot patterns
│   ├── PlayerInfoPane.java # Player cards with resource breakdown
│   ├── ActionPane.java   # Action buttons and status display
│   └── MarketPane.java   # Market price display
├── exception/      # Custom exceptions
└── util/           # Save/load and testing utilities
```

## Game Rules

### Setup Phase (Snake Draft)
- Players take turns placing one MVP + one Partnership (road)
- Draft order: forward then backward (e.g., 3 players: [0,1,2,2,1,0])

### Normal Phase
1. Roll two dice (2-12)
2. Sectors matching the roll produce resources for adjacent structures
3. Roll of 7 triggers Regulatory Crisis (discard if over limit, move auditor)
4. Build structures:
   - **MVP**: 1 Capital + 1 Talent + 1 Cloud + 1 Data → 1 point
   - **Partnership**: 1 Capital + 1 Patent → road connection
   - **Unicorn**: 3 Data + 2 Cloud (1 Cloud if Guru CTO) → upgrade MVP to 2 points

### Victory
First player to **10 points** wins.

### Founder Roles (optional, -1 point if chosen)
- **Hacker CEO**: 3:1 market trade rate (instead of 4:1)
- **Guru CTO**: Cheaper Unicorn upgrades (1 Cloud instead of 2)
- **VC Funded**: 9 card holding limit (instead of 7)

## Recent Changes

- Fixed dice display to show actual die values (was showing total/2)
- Added Canvas-drawn dice graphics with proper dot patterns
- Added per-resource breakdown with colored icons in player info
- Added bot player support with automatic play and visual feedback
- Fixed duplicate edge drawing on the board
- Added comprehensive test suite (37 tests, all passing)
- Added compile.bat and run-game.bat scripts
