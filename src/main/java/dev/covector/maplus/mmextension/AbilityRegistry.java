package dev.covector.maplus.mmextension;

import java.util.HashMap;

import dev.covector.maplus.mmextension.def.*;

public class AbilityRegistry {
    HashMap<String, Ability> abilities = new HashMap<String, Ability>();
    
    public AbilityRegistry() {
        add(new RelativeVelocity());
        add(new FlashBlind());
        add(new RaycastDirection());
        add(new QuestionAnswer());
        add(new SetGlow());
        add(new LookAt());
        add(new Pumpkin());
        add(new WardenSetTarget());
        add(new ResetStamina());
        add(new ResetMana());
        add(new EffectCleanse());
        add(new MLCooldownReset());
    }

    private void add(Ability ability) {
        abilities.put(ability.getId(), ability);
    }

    public Ability getAbility(String id) {
        return abilities.get(id);
    }
}