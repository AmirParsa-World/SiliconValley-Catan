@echo off
title Compile Silicon Valley Catan
set JAVA_HOME=C:\Users\silkroadnb\Downloads\jdk17-extracted\jdk-17.0.19+10
set JAVAC=%JAVA_HOME%\bin\javac.exe
set SRC=C:\Users\silkroadnb\Downloads\SiliconValley-Catan-master (1)\SiliconValley-Catan-master\src
set OUT=C:\Users\silkroadnb\Downloads\SiliconValley-Catan-master (1)\SiliconValley-Catan-master\out
set JFX=C:\Users\silkroadnb\Downloads\javafx-sdk\javafx-sdk-21.0.11\lib

echo Compiling...
if exist "%OUT%" rmdir /s /q "%OUT%"
mkdir "%OUT%"

"%JAVAC%" -encoding UTF-8 -d "%OUT%" -sourcepath "%SRC%" --module-path "%JFX%" --add-modules javafx.controls,javafx.fxml "%SRC%\Main.java" "%SRC%\model\Dice.java" "%SRC%\model\Sector.java" "%SRC%\model\Edge.java" "%SRC%\model\Map.java" "%SRC%\model\Vertex.java" "%SRC%\model\Unicorn.java" "%SRC%\model\Structure.java" "%SRC%\model\SimpleBot.java" "%SRC%\model\ResourceType.java" "%SRC%\model\Regulator.java" "%SRC%\model\Player.java" "%SRC%\model\MVP.java" "%SRC%\model\FounderRole.java" "%SRC%\controller\Market.java" "%SRC%\controller\GamePhase.java" "%SRC%\controller\GameEngine.java" "%SRC%\view\MainApp.java" "%SRC%\view\BoardCanvas.java" "%SRC%\view\PlayerInfoPane.java" "%SRC%\view\ActionPane.java" "%SRC%\view\DiscardDialog.java" "%SRC%\view\BotTestRunner.java" "%SRC%\view\DicePane.java" "%SRC%\view\MarketPane.java" "%SRC%\exception\StructurePlacementException.java" "%SRC%\exception\NotEnoughResourceException.java" "%SRC%\exception\InvalidTradeException.java" "%SRC%\exception\InvalidRoleException.java" "%SRC%\exception\InvalidPlacementException.java" "%SRC%\exception\InvalidAuditorPlacementException.java" "%SRC%\exception\AlreadyRolledException.java" "%SRC%\util\SaveManager.java" "%SRC%\util\PersistenceTester.java" "%SRC%\util\GameLogTester.java" "%SRC%\util\DeepPersistenceTester.java" "%SRC%\util\GuiSaveLoadTest.java" "%SRC%\util\MarketTradeTest.java"

if %ERRORLEVEL%==0 (
    echo Compilation successful!
) else (
    echo Compilation failed!
)
pause
