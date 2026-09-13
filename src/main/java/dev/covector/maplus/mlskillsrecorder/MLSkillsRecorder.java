package dev.covector.maplus.mlskillsrecorder;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.api.event.skill.PlayerCastSkillEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.player.cooldown.CooldownMap;

public class MLSkillsRecorder implements Listener {
    public static MLSkillsRecorder instance = null;
    public static MLSkillsRecorder GetInstance() {
        if (instance == null) {
            instance = new MLSkillsRecorder();
        }
        return instance;
    }

    public HashMap<UUID, SkillMetadata> lastSkill = new HashMap<UUID, SkillMetadata>();

    @EventHandler
    public void RecordSkill(PlayerCastSkillEvent event) {
        if (event.isCancelled()) { return; }
        lastSkill.put(event.getPlayer().getUniqueId(), event.getMetadata());
        // Bukkit.broadcastMessage("Player " + event.getPlayer().getName() + " casted skill " + event.getMetadata().getCast().getCooldownPath());
    }

    protected void resetSkillCooldown(Player caster, Skill skill) {
        CooldownMap cdm = MMOPlayerData.get(caster.getUniqueId()).getCooldownMap();
        if (cdm.getCooldown(skill) != 0) {
            cdm.resetCooldown(skill);
        }
    }

    public void castLastSkill(Player caster) {
        if (lastSkill.containsKey(caster.getUniqueId())) {
            SkillMetadata metadata = lastSkill.get(caster.getUniqueId());
            Skill skill = metadata.getCast();
            this.resetSkillCooldown(caster, skill);
            skill.cast(metadata);
        }
    }

    public void castLastSkill(Player caster, List<String> exceptions) {
        if (lastSkill.containsKey(caster.getUniqueId())) {
            SkillMetadata metadata = lastSkill.get(caster.getUniqueId());
            Skill skill = metadata.getCast();
            if (!exceptions.contains(skill.getCooldownPath())) {
                this.resetSkillCooldown(caster, skill);
                skill.cast(metadata);
            }
        }
    }

    public void unregister() {
        PlayerCastSkillEvent.getHandlerList().unregister(this);
    }
}
