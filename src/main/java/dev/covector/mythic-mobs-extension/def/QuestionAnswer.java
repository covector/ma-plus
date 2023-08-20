package dev.covector.maplus.mmextension;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.ChatColor;

import com.garbagemule.MobArena.framework.Arena;

import java.util.Random;
import java.util.Set;

import dev.covector.maplus.Utils;

public class QuestionAnswer extends Ability {
    private String syntax = "<caster-uuid> <min-complexity> <max-complexity> <base-time> <complexity-time-multiplier> <punishment-skill>";
    private String id = "questionAnswer";

    public String cast(String[] args) {
        if (args.length != 6) {
            return "args length must be 6";
        }

        Entity caster = MMExtUtils.parseUUID(args[0]);
        int minComplexity = Integer.parseInt(args[1]);
        int maxComplexity = Integer.parseInt(args[2]);
        double baseTime = Double.parseDouble(args[3]);
        double complexityTimeMultiplier = Double.parseDouble(args[4]);
        String punishmentSkill = args[5];

        if (!(caster instanceof LivingEntity)) {
            return "caster must be a living entity";
        }
        LivingEntity casterMob = (LivingEntity) caster;

        Question question = new Question(casterMob, minComplexity, maxComplexity, baseTime, complexityTimeMultiplier, punishmentSkill);

        return null;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }

    private class Question extends BukkitRunnable implements Listener {
        private String question;
        private String answer;

        private LivingEntity caster;
        private int minComplexity;
        private int maxComplexity;
        private double baseTime;
        private double complexityTimeMultiplier;
        private String punishmentSkill;

        private String casterName;

        private Random random = new Random();
        private int complexity;
        private int duration;
        private Set<Player> players;

        private int ti = 0;

        public Question(LivingEntity caster, int minComplexity, int maxComplexity, double baseTime, double complexityTimeMultiplier, String punishmentSkill) {
            this.caster = caster;
            this.minComplexity = minComplexity;
            this.maxComplexity = maxComplexity;
            this.baseTime = baseTime;
            this.complexityTimeMultiplier = complexityTimeMultiplier;
            this.punishmentSkill = punishmentSkill;

            this.casterName = caster.getCustomName();
            if (casterName == null) {
                casterName = caster.getName();
            }

            // get players
            Arena arena = Utils.getArenaWithMonster(caster);
            if (arena == null) {
                players = Set.copyOf(caster.getWorld().getPlayers());
            } else {
                players = arena.getPlayersInArena();
            }

            // start message
            generateQuestion();
            for (Player player : players) {
                player.sendMessage(getMessageStart(ChatColor.AQUA) + player.getName() + "-senpai, please teach me how to do: ");
                player.sendMessage(getMessageStart(ChatColor.AQUA) + "" + question);
                player.sendMessage(getMessageStart(ChatColor.AQUA) + "Onegaishimasu!");
            }

            runTaskTimer(Utils.getPlugin(), 0, 20);
            Bukkit.getPluginManager().registerEvents(this, Utils.getPlugin());
        }

        private void close() {
            cancel();
            AsyncPlayerChatEvent.getHandlerList().unregister(this);
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void a(AsyncPlayerChatEvent event) {
            if (players.contains(event.getPlayer())) {
                if (event.getMessage().equals(answer)) {
                    win(event.getPlayer());
                    close();
                }
            }
        }

        @Override
        public void run() {
            if (caster.isDead()) {
                close();
                return;
            }

            if (ti++ > duration) {
                lose();
                close();
                return;
            }

            // Count down
            if (duration - ti + 1 < 5) {
                tellPlayers(ChatColor.AQUA + String.valueOf(duration - ti + 2) + "...");
            }
        }

        private String getMessageStart(ChatColor color) {
            return color + "" + ChatColor.BOLD + casterName + " > " + ChatColor.RESET + "" + color;
        }

        private void tellPlayers(String message) {
            for (Player player : players) {
                player.sendMessage(message);
            }
        }

        private void win(Player winner) {
            Bukkit.getScheduler().runTaskLater(Utils.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    tellPlayers(getMessageStart(ChatColor.YELLOW) + winner.getName() + "-senpai, thanks for teaching me!");
                }
            }, 20);
        }

        private void lose() {
            for (Player player : players) {
                player.sendMessage(getMessageStart(ChatColor.DARK_RED) + "You are not helpful at all!");
                MMExtUtils.castMMSkill(caster, punishmentSkill, player, null);
            }
        }

        private void generateQuestion() {
            complexity = random.nextInt(maxComplexity - minComplexity + 1) + minComplexity;
            duration = (int) (baseTime + complexity * complexityTimeMultiplier);
            
            QuestionTree questionTree = new QuestionTree(random);
            int maxBracketLevel = -1;
            for (int i = 0; i < complexity; i++) {
                int bracketLevel = questionTree.addComplexity(random);
                if (bracketLevel > maxBracketLevel) {
                    maxBracketLevel = bracketLevel;
                }
            }

            question = questionTree.writeQuestion(maxBracketLevel);
            answer = String.valueOf(questionTree.evaluate());
        }

        private class QuestionTree {
            private Operator op;
            private QuestionTree left;
            private QuestionTree right;
            private int value;
            private int bracketLevel = -1;
            private QuestionTree parent;

            public QuestionTree(Random random) {
                this(random, null);
            }

            public QuestionTree(Random random, QuestionTree parent) {
                this(random.nextInt(10) + 1, parent);
            }

            public QuestionTree(int value, QuestionTree parent) {
                this.op = Operator.LEAF;
                this.value = value;
                this.parent = parent;
            }

            public int addComplexity(Random random) {
                if (op == Operator.LEAF) {
                    op = Operator.values()[random.nextInt(3)];
                    int BL = evaluateBracketLevel();
                    left = new QuestionTree(value, this);
                    value = 0;
                    right = new QuestionTree(random, this);
                    return BL;
                } else {
                    if (random.nextBoolean()) {
                        return left.addComplexity(random);
                    } else {
                        return right.addComplexity(random);
                    }
                }
            }

            private int evaluateBracketLevel() {
                if (parent == null) {
                    bracketLevel = -1;
                    return -1;
                }
                if (needBracket()) {
                    bracketLevel = parent.bracketLevel + 1;
                } else {
                    bracketLevel = parent.bracketLevel;
                }
                return bracketLevel;
            }

            public int evaluate() {
                switch (op) {
                    case ADD:
                        return left.evaluate() + right.evaluate();
                    case SUB:
                        return left.evaluate() - right.evaluate();
                    case MUL:
                        return left.evaluate() * right.evaluate();
                    case LEAF:
                        return value;
                }
                return 0;
            }

            private boolean needBracket() {
                if (parent == null) {
                    return false;
                }
                Operator parentOp = parent.op;
                return (parentOp == Operator.SUB || parentOp == Operator.MUL) && (op == Operator.ADD || op == Operator.SUB);
            }

            private ChatColor getBracketColor(int maxBracketLevel) {
                // return QuestionAnswer.bracketLevelColors[(maxBracketLevel - bracketLevel) % QuestionAnswer.bracketLevelColors.length];
                return QuestionAnswer.bracketLevelColors[bracketLevel % QuestionAnswer.bracketLevelColors.length];
            }

            public String writeQuestion(int maxBracketLevel) {
                String expression = op == Operator.LEAF ? 
                ChatColor.AQUA + String.valueOf(value) :
                left.writeQuestion(maxBracketLevel) + " " + ChatColor.RED + op.getSymbol() + " " + right.writeQuestion(maxBracketLevel);

                if (needBracket()) {
                    return getBracketColor(maxBracketLevel) + "(" + expression + getBracketColor(maxBracketLevel) + ")";
                } else {
                    return expression;
                }
            }
        }
    }
    
    enum Operator {
        ADD("+"), SUB("-"), MUL("x"), LEAF("");

        private String symbol;

        private Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    static ChatColor[] bracketLevelColors = new ChatColor[] {
        ChatColor.WHITE,
        ChatColor.LIGHT_PURPLE,
        ChatColor.YELLOW,
        ChatColor.DARK_GREEN,
        ChatColor.GOLD,
        ChatColor.BLUE,
        ChatColor.GRAY
    };
}
