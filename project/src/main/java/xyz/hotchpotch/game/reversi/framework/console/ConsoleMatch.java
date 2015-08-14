package xyz.hotchpotch.game.reversi.framework.console;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import xyz.hotchpotch.game.reversi.framework.GameResult;
import xyz.hotchpotch.game.reversi.framework.Match;
import xyz.hotchpotch.game.reversi.framework.Match.Entrant;
import xyz.hotchpotch.game.reversi.framework.MatchCondition;
import xyz.hotchpotch.game.reversi.framework.MatchResult;
import xyz.hotchpotch.game.reversi.framework.Player;
import xyz.hotchpotch.game.reversi.framework.console.ConsolePrinter.Level;
import xyz.hotchpotch.util.ConsoleScanner;

/**
 * 標準入出力を用いたマッチ実行クラスです。<br>
 * 
 * @author nmby
 */
public class ConsoleMatch implements ConsolePlayable<Match> {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    /**
     * マッチ条件を指定してマッチ実行クラスを生成します。<br>
     * 
     * @param matchCondition マッチ条件
     * @return マッチ実行クラス
     * @throws NullPointerException {@code matchCondition} が {@code null} の場合
     */
    public static ConsoleMatch of(MatchCondition matchCondition) {
        return new ConsoleMatch(Objects.requireNonNull(matchCondition));
    }
    
    /**
     * マッチ条件を標準入力から指定することによりマッチ実行クラスを生成します。<br>
     * 
     * @return マッチ実行クラス
     */
    public static ConsoleMatch arrange() {
        return new ConsoleMatch(arrangeMatchCondition());
    }
    
    private static MatchCondition arrangeMatchCondition() {
        Class<? extends Player> playerA = CommonUtil.arrangePlayerClass("プレーヤー" + Entrant.A);
        Class<? extends Player> playerB = CommonUtil.arrangePlayerClass("プレーヤー" + Entrant.B);
        long givenMillisPerTurn = CommonUtil.arrangeGivenMillisPerTurn();
        long givenMillisInGame = CommonUtil.arrangeGivenMillisInGame();
        int times = CommonUtil.arrangeTimes();
        Map<String, String> params = CommonUtil.arrangeAdditionalParams();
        
        return MatchCondition.of(playerA, playerB, givenMillisPerTurn, givenMillisInGame, times, params);
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    private final MatchCondition matchCondition;
    private final ConsolePrinter printer;
    private final ConsoleScanner<String> waiter = ConsoleScanner.waiter();
    
    private ConsoleMatch(MatchCondition matchCondition) {
        this.matchCondition = matchCondition;
        
        Level level = CommonUtil.getParameter(
                matchCondition,
                "print.level",
                s -> Enum.valueOf(Level.class, s),
                Level.MATCH);
        
        printer = ConsolePrinter.of(level);
    }
    
    /**
     * マッチを実行します。<br>
     * 
     * @return マッチ結果
     */
    @Override
    public synchronized MatchResult play() {
        printer.println(Level.MATCH, "");
        printer.println(Level.MATCH, "****************************************************************");
        printer.println(Level.MATCH, "マッチを開始します。");
        printer.print(Level.MATCH, matchCondition.toStringKindly());
        printer.println(Level.MATCH, "****************************************************************");
        printer.println(Level.MATCH, "");
        
        Map<Entrant, ConsoleGame> games = new EnumMap<>(Entrant.class);
        Map<Entrant, List<GameResult>> gameResults = new EnumMap<>(Entrant.class);
        for (Entrant entrant : Entrant.values()) {
            games.put(entrant, ConsoleGame.of(matchCondition.gameConditions.get(entrant)));
            gameResults.put(entrant, new ArrayList<>());
        }
        
        Entrant currEntrant = Entrant.A;
        for (int n = 0; n < matchCondition.times; n++) {
            
            ConsoleGame game = games.get(currEntrant);
            GameResult gameResult = game.play();
            gameResults.get(currEntrant).add(gameResult);
            
            currEntrant = currEntrant.opposite();
        }
        
        MatchResult matchResult = MatchResult.of(matchCondition, gameResults);
        
        printer.println(Level.MATCH, "****************************************************************");
        printer.println(Level.MATCH, "マッチが終了しました。");
        printer.println(Level.LEAGUE, matchResult.toString());
        printer.println(Level.MATCH, "****************************************************************");
        printer.println(Level.MATCH, "");
        if (printer.level == Level.MATCH) {
            waiter.get();
        }
        printer.println(Level.MATCH, "");
        
        return matchResult;
    }
}