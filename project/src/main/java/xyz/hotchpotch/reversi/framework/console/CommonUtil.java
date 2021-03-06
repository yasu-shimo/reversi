package xyz.hotchpotch.reversi.framework.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;

import xyz.hotchpotch.reversi.aiplayers.BreadthFirstAIPlayer;
import xyz.hotchpotch.reversi.aiplayers.CrazyAIPlayer;
import xyz.hotchpotch.reversi.aiplayers.DepthFirstAIPlayer;
import xyz.hotchpotch.reversi.aiplayers.MonteCarloAIPlayer;
import xyz.hotchpotch.reversi.aiplayers.RandomAIPlayer;
import xyz.hotchpotch.reversi.aiplayers.SimplestAIPlayer;
import xyz.hotchpotch.reversi.aiplayers.SlowpokeAIPlayer;
import xyz.hotchpotch.reversi.framework.Condition;
import xyz.hotchpotch.reversi.framework.Player;
import xyz.hotchpotch.util.console.ConsoleScanner;

/**
 * パッケージ内の共通的な処理を集めたユーティリティクラスです。<br>
 * 
 * @author nmby
 */
/*package*/ class CommonUtil {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private static final String BR = System.lineSeparator();
    
    /*package*/ static List<Class<? extends Player>> playerClasses() {
        // TODO: このバカチョン実装はそのうちどうにかしたい...
        return Arrays.asList(
                ConsolePlayer.class,
                SimplestAIPlayer.class,
                RandomAIPlayer.class,
                DepthFirstAIPlayer.class,
                BreadthFirstAIPlayer.class,
                MonteCarloAIPlayer.class,
                SlowpokeAIPlayer.class,
                CrazyAIPlayer.class);
    }
    
    /**
     * 標準入出力でプレーヤークラスの選択を促して選択結果を取得し、選択されたプレーヤークラスを返します。<br>
     * 
     * @param str プレーヤーの選択を促す際に表示する、プレーヤーの呼称。{@code "黒のプレーヤー"}、{@code "Player-A"}　など。
     * @param allowUserInput {@link NeedsUserInput} を実装するプレーヤークラスを選択肢に含める場合は {@code true}
     * @return 選択されたプレーヤークラス
     */
    /*package*/ static Class<? extends Player> arrangePlayerClass(String str, boolean allowUserInput) {
        assert str != null;
        
        List<Class<? extends Player>> playerClasses = new ArrayList<>(playerClasses());
        if (!allowUserInput) {
            playerClasses.removeIf(NeedsUserInput.class::isAssignableFrom);
        }
        StringBuilder prompt = new StringBuilder();
        prompt.append(String.format("%sを番号で選択してください。", str)).append(BR);
        for (int i = 0; i < playerClasses.size(); i++) {
            prompt.append(String.format("\t%d : %s", i + 1, playerClasses.get(i).getName())).append(BR);
        }
        prompt.append(String.format("\t0 : その他（自作クラス）")).append(BR);
        prompt.append("> ");
        
        ConsoleScanner<Integer> scIdx = ConsoleScanner
                .intBuilder(0, playerClasses.size())
                .prompt(prompt.toString())
                .build();
        int idx = scIdx.get();
        if (0 < idx) {
            return playerClasses.get(idx - 1);
        } else {
            return arrangeCustomPlayerClass(allowUserInput);
        }
    }
    
    /**
     * 標準入出力で任意のプレーヤークラスの指定を促して指定結果を取得し、指定されたプレーヤークラスを返します。<br>
     * 
     * @param allowUserInput {@link NeedsUserInput} を実装するプレーヤークラスの選択を許可する場合は {@code true}
     * @return 選択されたプレーヤークラス
     */
    private static Class<? extends Player> arrangeCustomPlayerClass(boolean allowUserInput) {
        Predicate<String> judge = s -> {
            try {
                @SuppressWarnings({ "unchecked", "unused" })
                Class<? extends Player> playerClass = (Class<? extends Player>) Class.forName(s);
                return true;
            } catch (ClassNotFoundException | ClassCastException e) {
                return false;
            }
        };
        Function<String, Class<? extends Player>> converter = s -> {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Player> playerClass = (Class<? extends Player>) Class.forName(s);
                return playerClass;
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            }
        };
        String prompt = "プレーヤークラスの完全修飾クラス名を指定してください（例：jp.co.hoge.MyAIPlayer）" + BR + "> ";
        String complaint = String.format(
                "クラスが見つからないか、%s を implements していません。", Player.class.getName()) + BR;
        ConsoleScanner<Class<? extends Player>> scPlayerClass = ConsoleScanner
                .builder(judge, converter, prompt, complaint).build();
                
        while (true) {
            Class<? extends Player> playerClass = scPlayerClass.get();
            if (allowUserInput || !NeedsUserInput.class.isAssignableFrom(playerClass)) {
                return playerClass;
            } else {
                System.out.println(String.format("%s を implements するクラスは指定できません。", NeedsUserInput.class.getSimpleName()));
            }
        }
    }
    
    /**
     * 標準入出力で複数のプレーヤークラスの選択を促して選択結果を取得し、選択されたプレーヤークラスのリストを返します。<br>
     * 
     * @param allowUserInput {@link NeedsUserInput} を実装するプレーヤークラスの選択を許可する場合は {@code true}
     * @return 選択されたプレーヤークラスのリスト
     */
    /*package*/ static List<Class<? extends Player>> arrangePlayerClassList(boolean allowUserInput) {
        List<Class<? extends Player>> playerClasses = new ArrayList<>(playerClasses());
        if (!allowUserInput) {
            playerClasses.removeIf(NeedsUserInput.class::isAssignableFrom);
        }
        Set<Integer> selected = new TreeSet<>();
        
        while (true) {
            StringBuilder prompt = new StringBuilder();
            prompt.append("選択するプレーヤーを番号で指定してください。選択済みのものを再度指定した場合は、選択を解除します。").append(BR)
                    .append("選択を終了する場合は -1 を入力してください。").append(BR);
                    
            for (int i = 0; i < playerClasses.size(); i++) {
                prompt.append(String.format("\t%s[%d] %s",
                        selected.contains(i) ? "★選択済み " : "",
                        i + 1,
                        playerClasses.get(i).getName())).append(BR);
            }
            prompt.append(String.format("\t[0] その他（自作クラス）")).append(BR);
            prompt.append("> ");
            
            ConsoleScanner<Integer> scIdx = ConsoleScanner
                    .intBuilder(-1, playerClasses.size())
                    .prompt(prompt.toString())
                    .build();
            int idx = scIdx.get();
            
            if (idx == -1) {
                if (2 <= selected.size()) {
                    break;
                } else {
                    System.out.println("2つ以上のプレーヤーを選択してください。");
                    System.out.println();
                }
            } else if (idx == 0) {
                Class<? extends Player> customPlayer = arrangeCustomPlayerClass(allowUserInput);
                playerClasses.add(customPlayer);
                selected.add(playerClasses.size() - 1);
            } else {
                if (selected.contains(idx - 1)) {
                    selected.remove(idx - 1);
                } else {
                    selected.add(idx - 1);
                }
            }
        }
        
        List<Class<? extends Player>> selectedPlayers = new ArrayList<>();
        for (int idx : selected) {
            selectedPlayers.add(playerClasses.get(idx));
        }
        return selectedPlayers;
    }
    
    /*package*/ static long arrangeGivenMillisPerTurn() {
        return ConsoleScanner
                .longBuilder(1, 60000)
                .prompt("一手あたりの制限時間（ミリ秒）を 1～60000（1分） の範囲で指定してください" + BR + "> ")
                .build()
                .get();
    }
    
    /*package*/ static long arrangeGivenMillisInGame() {
        return ConsoleScanner
                .longBuilder(1, 1800000)
                .prompt("ゲーム内での持ち時間（ミリ秒）を 1～1800000（30分） の範囲で指定してください" + BR + "> ")
                .build()
                .get();
    }
    
    /*package*/ static int arrangeTimes() {
        return ConsoleScanner
                .intBuilder(1, 100)
                .prompt("対戦回数を 1～100 の範囲で指定してください" + BR + "> ")
                .build()
                .get();
    }
    
    /*package*/ static boolean arrangeAuto() {
        int selected = ConsoleScanner
                .intBuilder(1, 2)
                .prompt("ゲームの進行方法を番号で選んでください（1: 自動進行, 2: 対話的逐次進行）" + BR + "> ")
                .build()
                .get();
        return selected == 1;
    }
    
    /*package*/ static boolean arrangeDispDetail() {
        int selected = ConsoleScanner
                .intBuilder(1, 2)
                .prompt("表示の詳細度を番号で選んでください（1: サマリのみ, 2: 詳細表示あり）" + BR + "> ")
                .build()
                .get();
        return selected == 2;
    }
    
    /*package*/ static Map<String, String> arrangeAdditionalParams(Map<String, String> params) {
        assert params != null;
        
        ConsoleScanner<String> sc = ConsoleScanner
                .stringBuilder("[^=]+=.+|^$")
                .prompt("追加のデバッグ用パラメータが必要な場合は key=value 形式で入力してください。" + BR
                        + "必要ない場合は何も入力せず Enter を押してください" + BR
                        + "> ")
                .build();
                
        while (true) {
            String str = sc.get();
            if ("".equals(str)) {
                break;
            }
            String[] keyValue = str.split("=", 2);
            params.put(keyValue[0].trim(), keyValue[1].trim());
        }
        
        return params;
    }
    
    /*package*/ static <T> T getParameter(
            Condition<?> condition,
            String key,
            Function<String, T> converter,
            T defaultValue) {
            
        assert condition != null;
        assert key != null;
        assert converter != null;
        
        if (!condition.getParams().containsKey(key)) {
            return defaultValue;
        }
        String str = condition.getParam(key);
        try {
            return converter.apply(str);
        } catch (RuntimeException e) {
            return defaultValue;
        }
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private CommonUtil() {
    }
}
