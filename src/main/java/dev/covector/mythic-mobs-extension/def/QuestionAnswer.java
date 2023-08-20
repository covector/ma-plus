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
                player.sendMessage(player.getName() + "-senpai, please teach me how to do: " + question);
                player.sendMessage("Onegaishimasu!");
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
                    win();
                    close();
                }
            }
        }

        @Override
        public void run() {
            if (ti++ > duration) {
                lose();
                close();
            }

            // Count down
            if (duration - ti < 5) {
                tellPlayers(String.valueOf(duration - ti + 1) + "...");
            }
        }

        private void tellPlayers(String message) {
            for (Player player : players) {
                player.sendMessage(message);
            }
        }

        private void win() {
            for (Player player : players) {
                player.sendMessage("Thanks for teaching me!");
            }
        }

        private void lose() {
            for (Player player : players) {
                player.sendMessage("You are not helpful a all!");
                MMExtUtils.castMMSkill(caster, punishmentSkill, player, null);
            }
        }

        private void generateQuestion() {
            complexity = random.nextInt(maxComplexity - minComplexity + 1) + minComplexity;
            duration = (int) (baseTime + complexity * complexityTimeMultiplier);
            
            QuestionTree questionTree = new QuestionTree(random);
            for (int i = 0; i < complexity; i++) {
                questionTree.addComplexity(random);
            }

            question = questionTree.toString();
            answer = String.valueOf(questionTree.evaluate());
        }

        private class QuestionTree {
            private Operator op;
            private QuestionTree left;
            private QuestionTree right;
            private int value;
            private int maxValue = 10;

            public QuestionTree(Random random) {
                this.op = Operator.LEAF;
                this.value = random.nextInt(maxValue) + 1;
            }

            public QuestionTree(int value) {
                this.op = Operator.LEAF;
                this.value = value;
            }

            public void addComplexity(Random random) {
                if (op == Operator.LEAF) {
                    op = Operator.values()[random.nextInt(2) + 1];
                    left = new QuestionTree(value);
                    value = 0;
                    right = new QuestionTree(random);
                } else {
                    if (random.nextBoolean()) {
                        left.addComplexity(random);
                    } else {
                        right.addComplexity(random);
                    }
                }
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

            public String toString(Operator parentOp) {
                switch (op) {
                    case ADD:
                        if (parentOp == Operator.SUB || parentOp == Operator.MUL) {
                            return "(" + left.toString() + " + " + right.toString() + ")";
                        } else {
                            return left.toString() + " + " + right.toString();
                        }
                    case SUB:
                        if (parentOp == Operator.SUB || parentOp == Operator.MUL) {
                            return "(" + left.toString() + " - " + right.toString() + ")";
                        } else {
                            return left.toString() + " - " + right.toString();
                        }
                    case MUL:
                        return left.toString() + " x " + right.toString();
                    case LEAF:
                        return Integer.toString(value);
                }
                return "";
            }
        }
    }
    
    enum Operator {
        ADD, SUB, MUL, LEAF
    }
}
