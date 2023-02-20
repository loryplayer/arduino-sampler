package arduino;

import javafx.application.Platform;

import java.util.Timer;
import java.util.TimerTask;

import static interfaccia.IndexController.PRIMARY_CONTROLLER;

/**
 * <p>Classe TimerController, utilizzata per effettuare i campionamenti.</p>
 * Metodi principali:
 * <ul style="margin-top: 0px">
 *     <li>
 *         {@link #start()}
 *     </li>
 *     <li>
 *         {@link #stop()}
 *     </li>
 * </ul>
 */
public class TimerController {
    /**
     * Oggetto {@link Timer}
     */
    private Timer timer;

    /**
     * Metodo utilizzato per avviare il campionamento
     *
     * @see Controller
     * @see Serial#getSamplingSettings()
     */
    public void start() {
        this.timer = new Timer();
        Controller timerController = new Controller();
        timer.schedule(timerController, 0, (long) PRIMARY_CONTROLLER.getSerialSelected().getSamplingSettings().getPeriod_ms());
        PRIMARY_CONTROLLER.getLogger().writeWithTime("Campionamento avviato...");
    }

    /**
     * Metodo utilizzato per arrestare il campionamento
     */
    public void stop() {
        if (this.timer != null) {
            this.timer.cancel();
            PRIMARY_CONTROLLER.getLogger().write(String.format("Numero campionamenti effettuati: %d", PRIMARY_CONTROLLER.getSerialSelected().getDataCollectorCount()));
            PRIMARY_CONTROLLER.getLogger().writeWithTime("Campionamento fermato...");
        }
    }

}

/**
 * Classe Controller, estende la classe {@link TimerTask}.
 * <p>Metodi principali:</p>
 * <ul style="margin-top: 0px">
 *     <li>
 *         {@link #run()}
 *     </li>
 * </ul>
 */
class Controller extends TimerTask {

    /**
     * Override al metodo {@link TimerTask#run()}.
     * <p>
     * Questo metodo viene richiamato a ogni intervallo di campionamento.
     *
     * @see SamplingSettings
     * </p>
     */
    @Override
    public void run() {
        Platform.runLater(() -> {
            PRIMARY_CONTROLLER.getSerialSelected().read_and_collect();
            PRIMARY_CONTROLLER.refreshChart();
        });
    }
}
