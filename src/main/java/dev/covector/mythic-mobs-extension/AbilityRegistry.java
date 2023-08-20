package dev.covector.maplus.mmextension;

import java.util.HashMap;

public class AbilityRegistry {
    HashMap<String, Ability> abilities = new HashMap<String, Ability>();
    
    public AbilityRegistry() {
        add(new RelativeVelocity());
        add(new FlashBlind());
        add(new RaycastDirection());
        add(new QuestionAnswer());
        add(new SetGlow());
    }

    private void add(Ability ability) {
        abilities.put(ability.getId(), ability);
    }

    public Ability getAbility(String id) {
        return abilities.get(id);
    }
}