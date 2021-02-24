package ovh.roro.wankil.deathswap.game.json;

import com.google.gson.annotations.SerializedName;

public final class JsonSwaps {

    private JsonSwapStep[] steps;

    @SerializedName("timer_reveal")
    private int timerReveal;

    public JsonSwapStep[] getSteps() {
        return this.steps;
    }

    public int getTimerReveal() {
        return this.timerReveal;
    }

    public void setTimerReveal(int timerReveal) {
        this.timerReveal = timerReveal;
    }
}
