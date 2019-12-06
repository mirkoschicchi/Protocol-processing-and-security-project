package fi.utu.protproc.group3.protocols.bgp4.fsm;

import fi.utu.protproc.group3.protocols.bgp4.BGP4Message;
import fi.utu.protproc.group3.protocols.bgp4.BGP4MessageNotification;
import fi.utu.protproc.group3.protocols.bgp4.BGPPeerContext;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class BGPStateMachine extends AbstractStateMachine<BGPStateMachine, BGPStateMachine.State, BGPStateMachine.Event, BGPStateMachine.EventContext> {
    public static final short DEFAULT_HOLD_TIME = 90;

    private static final Logger LOGGER = Logger.getLogger(BGPStateMachine.class.getName());
    private static final StateMachineBuilder<BGPStateMachine, State, Event, EventContext> builder;

    static {
        builder = StateMachineBuilderFactory.create(
                BGPStateMachine.class, State.class, Event.class, EventContext.class,
                BGPCallbacks.class);

        // IDLE
        {
            builder.onEntry(State.Idle).callMethod("onIdle");
            builder.externalTransition().from(State.Idle).to(State.Connect).on(Event.ManualStart).callMethod("onManualStart_AutomaticStartIdle");
            builder.externalTransition().from(State.Idle).to(State.Connect).on(Event.AutomaticStart).callMethod("onManualStart_AutomaticStartIdle");
            builder.externalTransition().from(State.Idle).to(State.Active).on(Event.ManualStart_with_PassiveTcpEstablishment).callMethod("onManualStart_with_PassiveTcpEstablishment_AutomaticStart_with_PassiveTcpEstablishmentIdle");
            builder.externalTransition().from(State.Idle).to(State.Active).on(Event.AutomaticStart_with_PassiveTcpEstablishment).callMethod("onManualStart_with_PassiveTcpEstablishment_AutomaticStart_with_PassiveTcpEstablishmentIdle");
        }

        // CONNECT
        {
            builder.onEntry(State.Connect).callMethod("onConnect");
            builder.externalTransition().from(State.Connect).to(State.Idle).on(Event.ManualStop).callMethod("onManualStopConnect");
            builder.internalTransition().within(State.Connect).on(Event.ConnectRetryTimer_Expires).callMethod("onConnectRetryTimer_ExpiresConnect");
            builder.externalTransition().from(State.Connect).to(State.OpenSent).on(Event.DelayOpenTimer_Expires).callMethod("onDelayOpenTimer_ExpiresConnect");
            builder.externalTransition().from(State.Connect).to(State.OpenSent).on(Event.Tcp_CR_Acked).callMethod("onTcp_CR_Acked_TcpConnectionConfirmedConnect");
            builder.externalTransition().from(State.Connect).to(State.OpenSent).on(Event.TcpConnectionConfirmed).callMethod("onTcp_CR_Acked_TcpConnectionConfirmedConnect");

            builder.externalTransition().from(State.Connect).to(State.OpenConfirm).on(Event.BGPOpen_with_DelayOpenTimer_running).callMethod("onBGPOpen_with_DelayOpenTimer_runningConnect");
            builder.externalTransition().from(State.Connect).to(State.Idle).on(Event.BGPHeaderErr).callMethod("onBGPHeaderErrConnect");
            builder.externalTransition().from(State.Connect).to(State.Idle).on(Event.BGPOpenMsgErr).callMethod("onBGPOpenMsgErrConnect");
            builder.externalTransition().from(State.Connect).to(State.Idle).on(Event.NotifMsgVerErr).callMethod("onNotifMsgVerErrConnect");
            builder.externalTransition().from(State.Connect).to(State.Idle).on(Event.AutomaticStop).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from(State.Connect).to(State.Idle).on(Event.HoldTimer_Expires).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from(State.Connect).to(State.Idle).on(Event.KeepaliveTimer_Expires).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from(State.Connect).to(State.Idle).on(Event.IdleHoldTimer_Expires).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from(State.Connect).to(State.Idle).on(Event.BGPOpen).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from(State.Connect).to(State.Idle).on(Event.OpenCollisionDump).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from(State.Connect).to(State.Idle).on(Event.NotifMsg).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from(State.Connect).to(State.Idle).on(Event.KeepAliveMsg).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from(State.Connect).to(State.Idle).on(Event.UpdateMsg).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from(State.Connect).to(State.Idle).on(Event.UpdateMsgErr).callMethod("onAnyOtherEventConnect");
        }

        // ACTIVE
        {
            builder.onEntry(State.Active).callMethod("onActive");
            builder.externalTransition().from(State.Active).to(State.Idle).on(Event.ManualStop).callMethod("onManualStopActive");
            builder.externalTransition().from(State.Active).to(State.Connect).on(Event.ConnectRetryTimer_Expires).callMethod("onConnectRetryTimer_ExpiresActive");
            builder.externalTransition().from(State.Active).to(State.OpenSent).on(Event.DelayOpenTimer_Expires).callMethod("onDelayOpenTimer_ExpiresActive");
            builder.internalTransition().within(State.Active).on(Event.TcpConnection_Valid).callMethod("onTcpConnection_ValidActive");
            builder.internalTransition().within(State.Active).on(Event.Tcp_CR_Invalid).callMethod("onTcp_CR_InvalidActive");
            builder.externalTransition().from(State.Active).to(State.Idle).on(Event.TcpConnectionFails).callMethod("onTcpConnectionFailsActive");
            builder.externalTransition().from(State.Active).to(State.OpenConfirm).on(Event.BGPOpen_with_DelayOpenTimer_running).callMethod("onBGPOpen_with_DelayOpenTimer_runningActive");
            builder.externalTransition().from(State.Active).to(State.Idle).on(Event.BGPHeaderErr).callMethod("onBGPHeaderErrActive");
            builder.externalTransition().from(State.Active).to(State.Idle).on(Event.BGPOpenMsgErr).callMethod("onBGPOpenMsgErrActive");
            builder.externalTransition().from(State.Active).to(State.Idle).on(Event.NotifMsgVerErr).callMethod("onNotifMsgVerErrActive");
            builder.externalTransition().from(State.Active).to(State.Idle).on(Event.AutomaticStop).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from(State.Active).to(State.Idle).on(Event.HoldTimer_Expires).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from(State.Active).to(State.Idle).on(Event.KeepaliveTimer_Expires).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from(State.Active).to(State.Idle).on(Event.IdleHoldTimer_Expires).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from(State.Active).to(State.Idle).on(Event.BGPOpen).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from(State.Active).to(State.Idle).on(Event.OpenCollisionDump).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from(State.Active).to(State.Idle).on(Event.NotifMsg).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from(State.Active).to(State.Idle).on(Event.KeepAliveMsg).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from(State.Active).to(State.Idle).on(Event.UpdateMsg).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from(State.Active).to(State.Idle).on(Event.UpdateMsgErr).callMethod("onAnyOtherEventActive");
        }

        // OPEN_SENT
        {
            builder.onEntry(State.OpenSent).callMethod("onOpenSent");
            builder.externalTransition().from(State.OpenSent).to(State.Idle).on(Event.ManualStop).callMethod("onManualStopOpenSent");
            builder.externalTransition().from(State.OpenSent).to(State.Idle).on(Event.AutomaticStop).callMethod("onAutomaticStopOpenSent");
            builder.externalTransition().from(State.OpenSent).to(State.Idle).on(Event.HoldTimer_Expires).callMethod("onHoldTimer_ExpiresOpenSent");
            builder.externalTransition().from(State.OpenSent).to(State.Active).on(Event.TcpConnectionFails).callMethod("onTcpConnectionFailsOpenSent");
            builder.externalTransition().from(State.OpenSent).to(State.OpenConfirm).on(Event.BGPOpen).callMethod("onBGPOpenOpenSent");
            builder.externalTransition().from(State.OpenSent).to(State.Idle).on(Event.BGPHeaderErr).callMethod("onBGPHeaderErrOpenSent");
            builder.externalTransition().from(State.OpenSent).to(State.Idle).on(Event.BGPOpenMsgErr).callMethod("onBGPOpenMsgErrOpenSent");
            builder.externalTransition().from(State.OpenSent).to(State.Idle).on(Event.OpenCollisionDump).callMethod("onOpenCollisionDumpOpenSent");
            builder.externalTransition().from(State.OpenSent).to(State.Idle).on(Event.NotifMsgVerErr).callMethod("onNotifMsgVerErrOpenSent");
            builder.externalTransition().from(State.OpenSent).to(State.Idle).on(Event.ConnectRetryTimer_Expires).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from(State.OpenSent).to(State.Idle).on(Event.KeepaliveTimer_Expires).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from(State.OpenSent).to(State.Idle).on(Event.DelayOpenTimer_Expires).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from(State.OpenSent).to(State.Idle).on(Event.IdleHoldTimer_Expires).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from(State.OpenSent).to(State.Idle).on(Event.BGPOpen_with_DelayOpenTimer_running).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from(State.OpenSent).to(State.Idle).on(Event.NotifMsg).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from(State.OpenSent).to(State.Idle).on(Event.KeepAliveMsg).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from(State.OpenSent).to(State.Idle).on(Event.UpdateMsg).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from(State.OpenSent).to(State.Idle).on(Event.UpdateMsgErr).callMethod("onAnyOtherEventOpenSent");
        }

        // OPEN_CONFIRM
        {
            builder.onEntry(State.OpenConfirm).callMethod("onOpenConfirm");
            builder.externalTransition().from(State.OpenConfirm).to(State.Idle).on(Event.ManualStop).callMethod("onManualStopOpenConfirm");
            builder.externalTransition().from(State.OpenConfirm).to(State.Idle).on(Event.AutomaticStop).callMethod("onAutomaticStopOpenConfirm");
            builder.externalTransition().from(State.OpenConfirm).to(State.Idle).on(Event.HoldTimer_Expires).callMethod("onHoldTimer_ExpiresOpenConfirm");
            builder.internalTransition().within(State.OpenConfirm).on(Event.KeepaliveTimer_Expires).callMethod("onKeepaliveTimer_ExpiresOpenConfirm");
            builder.externalTransition().from(State.OpenConfirm).to(State.Idle).on(Event.TcpConnectionFails).callMethod("onTcpConnectionFails_NotifMsgOpenConfirm");
            builder.externalTransition().from(State.OpenConfirm).to(State.Idle).on(Event.NotifMsg).callMethod("onTcpConnectionFails_NotifMsgOpenConfirm");
            builder.externalTransition().from(State.OpenConfirm).to(State.Idle).on(Event.NotifMsgVerErr).callMethod("onNotifMsgVerErrOpenConfirm");
            builder.externalTransition().from(State.OpenConfirm).to(State.Idle).on(Event.BGPHeaderErr).callMethod("onBGPHeaderErrOpenConfirm");
            builder.externalTransition().from(State.OpenConfirm).to(State.Idle).on(Event.BGPOpenMsgErr).callMethod("onBGPOpenMsgErrOpenConfirm");
            builder.externalTransition().from(State.OpenConfirm).to(State.Idle).on(Event.OpenCollisionDump).callMethod("onOpenCollisionDumpOpenConfirm");
            builder.externalTransition().from(State.OpenConfirm).to(State.Established).on(Event.KeepAliveMsg).callMethod("onKeepAliveMsgOpenConfirm");
            builder.externalTransition().from(State.OpenConfirm).to(State.Idle).on(Event.ConnectRetryTimer_Expires).callMethod("onAnyOtherEventOpenConfirm");
            builder.externalTransition().from(State.OpenConfirm).to(State.Idle).on(Event.DelayOpenTimer_Expires).callMethod("onAnyOtherEventOpenConfirm");
            builder.externalTransition().from(State.OpenConfirm).to(State.Idle).on(Event.IdleHoldTimer_Expires).callMethod("onAnyOtherEventOpenConfirm");
            builder.externalTransition().from(State.OpenConfirm).to(State.Idle).on(Event.BGPOpen_with_DelayOpenTimer_running).callMethod("onAnyOtherEventOpenConfirm");
            builder.externalTransition().from(State.OpenConfirm).to(State.Idle).on(Event.UpdateMsg).callMethod("onAnyOtherEventOpenConfirm");
            builder.externalTransition().from(State.OpenConfirm).to(State.Idle).on(Event.UpdateMsgErr).callMethod("onAnyOtherEventOpenConfirm");
        }

        // ESTABLISHED
        {
            builder.onEntry(State.Established).callMethod("onEstablished");
            builder.externalTransition().from(State.Established).to(State.Idle).on(Event.ManualStop).callMethod("onManualStopEstablished");
            builder.externalTransition().from(State.Established).to(State.Idle).on(Event.AutomaticStop).callMethod("onAutomaticStopEstablished");
            builder.externalTransition().from(State.Established).to(State.Idle).on(Event.HoldTimer_Expires).callMethod("onHoldTimer_ExpiresEstablished");
            builder.internalTransition().within(State.Established).on(Event.KeepaliveTimer_Expires).callMethod("onKeepaliveTimer_ExpiresEstablished");
            builder.externalTransition().from(State.Established).to(State.Idle).on(Event.OpenCollisionDump).callMethod("onOpenCollisionDumpEstablished");
            builder.externalTransition().from(State.Established).to(State.Idle).on(Event.TcpConnectionFails).callMethod("onTcpConnectionFails_NotifMsgVerErr_NotifMsgEstablished");
            builder.externalTransition().from(State.Established).to(State.Idle).on(Event.NotifMsgVerErr).callMethod("onTcpConnectionFails_NotifMsgVerErr_NotifMsgEstablished");
            builder.externalTransition().from(State.Established).to(State.Idle).on(Event.NotifMsg).callMethod("onTcpConnectionFails_NotifMsgVerErr_NotifMsgEstablished");
            builder.internalTransition().within(State.Established).on(Event.KeepAliveMsg).callMethod("onKeepAliveMsgEstablished");
            builder.internalTransition().within(State.Established).on(Event.UpdateMsg).callMethod("onUpdateMsgEstablished");
            builder.externalTransition().from(State.Established).to(State.Idle).on(Event.UpdateMsgErr).callMethod("onUpdateMsgErrEstablished");
            builder.externalTransition().from(State.Established).to(State.Idle).on(Event.ConnectRetryTimer_Expires).callMethod("onAnyOtherEventEstablished");
            builder.externalTransition().from(State.Established).to(State.Idle).on(Event.DelayOpenTimer_Expires).callMethod("onAnyOtherEventEstablished");
            builder.externalTransition().from(State.Established).to(State.Idle).on(Event.IdleHoldTimer_Expires).callMethod("onAnyOtherEventEstablished");
            builder.externalTransition().from(State.Established).to(State.Idle).on(Event.BGPOpen_with_DelayOpenTimer_running).callMethod("onAnyOtherEventEstablished");
            builder.externalTransition().from(State.Established).to(State.Idle).on(Event.BGPHeaderErr).callMethod("onAnyOtherEventEstablished");
            builder.externalTransition().from(State.Established).to(State.Idle).on(Event.BGPOpenMsgErr).callMethod("onAnyOtherEventEstablished");
        }
    }

    public static BGPStateMachine newInstance(BGPCallbacks callbacks) {
        return builder.newStateMachine(State.Idle, callbacks);
    }

    public enum Event {
        ManualStart, ManualStop, AutomaticStart, ManualStart_with_PassiveTcpEstablishment,
        AutomaticStart_with_PassiveTcpEstablishment, AutomaticStart_with_DampPeerOscillations,
        AutomaticStart_with_DampPeerOscillations_and_PassiveTcpEstablishment,
        AutomaticStop, ConnectRetryTimer_Expires, HoldTimer_Expires, KeepaliveTimer_Expires,
        DelayOpenTimer_Expires, IdleHoldTimer_Expires, TcpConnection_Valid, Tcp_CR_Invalid,
        Tcp_CR_Acked, TcpConnectionConfirmed, TcpConnectionFails, BGPOpen,
        BGPOpen_with_DelayOpenTimer_running, BGPHeaderErr, BGPOpenMsgErr, OpenCollisionDump,
        NotifMsgVerErr, NotifMsg, KeepAliveMsg, UpdateMsg, UpdateMsgErr
    }

    public enum State {
        Idle, Connect, Active, OpenSent, OpenConfirm, Established,
    }

    static class EventContext {
        private final BGPPeerContext context;
        private final BGP4Message message;

        EventContext(BGPPeerContext context, BGP4Message message) {
            this.context = context;
            this.message = message;
        }

        public BGPPeerContext getContext() {
            return context;
        }

        public BGP4Message getMessage() {
            return message;
        }
    }

    private int connectRetryCounter = 0;
    private int connectRetryTime = 120;
    private int holdTime = DEFAULT_HOLD_TIME;
    private int keepaliveTime = 30;

    private Timer connectRetryTimer = new Timer();
    private Timer holdTimer = new Timer();
    private Timer keepaliveTimer = new Timer();

    // Optional attributes
    private int delayOpenTime = 120; // Not sure
    private Timer delayOpenTimer = new Timer();
    boolean delayOpen = true;
    private final boolean dampPeerOscillations = false;
    private final boolean sendNOTIFICATIONwithoutOPEN = false;

    private final BGPCallbacks callbacks;

    private BGPStateMachine(BGPCallbacks callbacks) {
        Objects.requireNonNull(callbacks);

        this.callbacks = callbacks;
    }

    private void restartConnectRetryTimer() {
        connectRetryTimer.cancel();
        connectRetryTimer = new Timer();

        connectRetryTime = 120;
        TimerTask connectRetryTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (connectRetryTime > 1) {
                    connectRetryTime--;
                    LOGGER.finest("Connect Retry Timer: " + connectRetryTime);
                } else {
                    // stop the timer
                    fire(Event.ConnectRetryTimer_Expires);
                }
            }
        };

        connectRetryTimer.schedule(connectRetryTimerTask, 0, 1000);
    }

    private void restartDelayOpenTimer() {
        delayOpenTimer.cancel();
        delayOpenTimer = new Timer();

        delayOpenTime = 120;
        TimerTask delayOpenTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (delayOpenTime > 0) {
                    delayOpenTime--;
                    LOGGER.finest("Delay Open Timer: " + delayOpenTime);
                } else {
                    // stop the timer
                    fire(Event.DelayOpenTimer_Expires);
                }
            }
        };

        delayOpenTimer.schedule(delayOpenTimerTask, 0, 1000);
    }

    private void restartHoldTimer(int time) {
        holdTimer.cancel();
        holdTimer = new Timer();

        holdTime = time;

        TimerTask holdTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (holdTime > 0) {
                    holdTime--;
                    LOGGER.finest("Hold Timer: " + holdTime);
                } else {
                    // stop the timer
                    fire(Event.HoldTimer_Expires);
                }
            }
        };

        holdTimer.schedule(holdTimerTask, 0, 1000);
    }

    private void restartKeepaliveTimer() {
        keepaliveTimer.cancel();
        keepaliveTimer = new Timer();

        keepaliveTime = 30;

        TimerTask keepaliveTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (keepaliveTime > 0) {
                    keepaliveTime--;
                    LOGGER.finest("Keepalive Timer: " + keepaliveTime);
                } else {
                    // stop the timer
                    fire(Event.KeepaliveTimer_Expires);
                }
            }
        };

        keepaliveTimer.schedule(keepaliveTimerTask, 0, 1000);
    }

    // IDLE -------------------------------------------------------------------------------------------------------
    // @AsyncExecute
    public void onManualStart_AutomaticStartIdle(State from, State to, Event event, EventContext context) {
        // initializes all BGP resources for the peer connection
        callbacks.initializeBGPResources();

        // sets ConnectRetryCounter to zero
        connectRetryCounter = 0;

        // starts the ConnectRetryTimer with the initial value
        restartConnectRetryTimer();

        // initiates a TCP connection to the other BGP peer
        callbacks.connectRemotePeer();

        // listens for a connection that may be initiated by the remote BGP peer
        callbacks.listenForTCPConnection();

        // changes its state to Connect
        LOGGER.fine("Changing state from IDLE to CONNECT");
    }

    // @AsyncExecute
    public void onManualStart_with_PassiveTcpEstablishment_AutomaticStart_with_PassiveTcpEstablishmentIdle(State from, State to, Event event, EventContext context) {
        // initializes all BGP resources
        callbacks.initializeBGPResources();

        // sets the ConnectRetryCounter to zero
        connectRetryCounter = 0;

        // starts the ConnectRetryTimer with the initial value
        restartConnectRetryTimer();

        // listens for a connection that may be initiated by the remote peer
        callbacks.listenForTCPConnection();

        // changes its state to Active
        LOGGER.fine("Changing state from IDLE to ACTIVE");
    }
    // ------------------------------------------------------------------------------------------------------------

    // CONNECT ----------------------------------------------------------------------------------------------------
    // @AsyncExecute
    public void onManualStopConnect(State from, State to, Event event, EventContext context) {
        // drops the TCP connection
        callbacks.dropTCPConnection();

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // sets ConnectRetryCounter to zero
        connectRetryCounter = 0;

        // stops the ConnectRetryTimer and sets ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;


        // Change the state to IDLE
        LOGGER.fine("Changing state from CONNECT to IDLE");
    }

    // @AsyncExecute
    public void onConnectRetryTimer_ExpiresConnect(State from, State to, Event event, EventContext context) {
        // drops the TCP connection
        callbacks.dropTCPConnection();

        // restarts the ConnectRetryTimer
        restartConnectRetryTimer();

        // stops the DelayOpenTimer and resets the timer to zero{
        delayOpenTimer.cancel();
        delayOpenTime = 0;

        // initiates a TCP connection to the other BGP peer
        callbacks.connectRemotePeer();

        // continues to listen for a connection that may be initiated by the remote BGP peer
        callbacks.listenForTCPConnection();

        // stays in the Connect state
        LOGGER.fine("Remaining in CONNECT state");
    }

    // @AsyncExecute
    public void onDelayOpenTimer_ExpiresConnect(State from, State to, Event event, EventContext context) {
        // sends an OPEN message to its peer
        callbacks.sendOpenMessage();

        // sets the HoldTimer to a large value
        restartHoldTimer(240);

        // changes its state to OpenSent
        LOGGER.fine("Changing state from CONNECT to OPEN_SENT");
    }

    // @AsyncExecute
    public void onTcp_CR_Acked_TcpConnectionConfirmed_DelayOpenConnect(State from, State to, Event event, EventContext context) {
        // stops the ConnectRetryTimer (if running) and sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // sets the DelayOpenTimer to the initial value
        restartDelayOpenTimer();

        // stays in the Connect state
        LOGGER.fine("Remaining in CONNECT state");
    }

    // @AsyncExecute
    public void onTcp_CR_Acked_TcpConnectionConfirmedConnect(State from, State to, Event event, EventContext context) {
        // stops the ConnectRetryTimer (if running) and sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // completes BGP initialization
        callbacks.completeBGPPeerInitialization();

        // sends an OPEN message to its peer
        callbacks.sendOpenMessage();

        // sets the HoldTimer to a large value
        restartHoldTimer(240);

        // changes its state to OpenSent
        LOGGER.fine("Changing state from CONNECT to OPEN_SENT");
    }

    // TODO: If the TCP connection fails (Event 18), the local system checks
    //      the DelayOpenTimer.  If the DelayOpenTimer is running, the local
    //      system:
    //        - restarts the ConnectRetryTimer with the initial value,
    //        - stops the DelayOpenTimer and resets its value to zero,
    //        - continues to listen for a connection that may be initiated by
    //          the remote BGP peer, and
    //        - changes its state to Active.
    //      If the DelayOpenTimer is not running, the local system:
    //      - stops the ConnectRetryTimer to zero,
    //        - drops the TCP connection,
    //        - releases all BGP resources, and
    //        - changes its state to Idle.

    // @AsyncExecute
    public void onBGPOpen_with_DelayOpenTimer_runningConnect(State from, State to, Event event, EventContext context) {
        // stops the ConnectRetryTimer (if running) and sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;


        // completes the BGP initialization
        callbacks.completeBGPPeerInitialization();

        // stops and clears the DelayOpenTimer (sets the value to zero)
        delayOpenTimer.cancel();
        delayOpenTime = 0;


        // sends an OPEN message
        callbacks.sendOpenMessage();

        // sends a KEEPALIVE message
        callbacks.sendKeepaliveMessage();

        if (holdTime != 0) {
            // starts the KeepaliveTimer with the initial value
            restartKeepaliveTimer();

            // resets the HoldTimer to the negotiated value
            restartHoldTimer(90);
        } else {
            // resets the KeepaliveTimer
            keepaliveTimer.cancel();
            keepaliveTime = 0;

            // resets the HoldTimer value to zero
            holdTimer.cancel();
            holdTime = 0;
        }

        // and changes its state to OpenConfirm
        LOGGER.fine("Changing state from CONNECT to OPEN_CONFIRM");
    }

    // @AsyncExecute
    public void onBGPHeaderErrConnect(State from, State to, Event event, EventContext context) {
        // (optionally) If the SendNOTIFICATIONwithoutOPEN attribute is
        //          set to TRUE, then the local system first sends a NOTIFICATION
        //          message with the appropriate error code
        if (sendNOTIFICATIONwithoutOPEN) {
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_MESSAGE_HEADER_ERROR, (byte) 0, null);
        }

        // stops the ConnectRetryTimer (if running) and sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from CONNECT to IDLE");
    }

    // @AsyncExecute
    public void onBGPOpenMsgErrConnect(State from, State to, Event event, EventContext context) {
        // (optionally) If the SendNOTIFICATIONwithoutOPEN attribute is
        //          set to TRUE, then the local system first sends a NOTIFICATION
        //          message with the appropriate error code
        if (sendNOTIFICATIONwithoutOPEN) {
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_OPEN_MESSAGE_ERROR, (byte) 0, null);
        }

        // stops the ConnectRetryTimer (if running) and sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from CONNECT to IDLE");
    }

    // @AsyncExecute
    public void onNotifMsgVerErrConnect(State from, State to, Event event, EventContext context) {
        if (delayOpenTime > 0) {
            // stops the ConnectRetryTimer (if running) and sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // stops and resets the DelayOpenTimer (sets to zero)
            delayOpenTimer.cancel();
            delayOpenTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // changes its state to Idle
            LOGGER.fine("Changing state from CONNECT to IDLE");
        } else {
            // stops the ConnectRetryTimer and sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // performs peer oscillation damping if the DampPeerOscillations attribute is set to True
            if (dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from CONNECT to IDLE");
        }
    }

    // @AsyncExecute
    public void onAnyOtherEventConnect(State from, State to, Event event, EventContext context) {
        // if the ConnectRetryTimer is running, stops and resets the ConnectRetryTimer (sets to zero)
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // if the DelayOpenTimer is running, stops and resets the DelayOpenTimer (sets to zero)
        delayOpenTimer.cancel();
        delayOpenTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // performs peer oscillation damping if the DampPeerOscillations attribute is set to True
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from CONNECT to IDLE");
    }
    // ------------------------------------------------------------------------------------------------------------

    // ACTIVE -----------------------------------------------------------------------------------------------------
    // @AsyncExecute
    public void onManualStopActive(State from, State to, Event event, EventContext context) {
        // If the DelayOpenTimer is running and the
        // SendNOTIFICATIONwithoutOPEN session attribute is set, the
        // local system sends a NOTIFICATION with a Cease
        if (delayOpenTime > 0 && delayOpenTime < 120 && sendNOTIFICATIONwithoutOPEN) {
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE, (byte) 0, null);
        }

        // releases all BGP resources including stopping the DelayOpenTimer
        callbacks.releaseBGPResources();
        delayOpenTimer.cancel();
        delayOpenTime = 0;

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // sets ConnectRetryCounter to zero
        connectRetryCounter = 0;

        // stops the ConnectRetryTimer and sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // changes its state to Idle
        LOGGER.fine("Changing state from ACTIVE to IDLE");
    }

    // @AsyncExecute
    public void onConnectRetryTimer_ExpiresActive(State from, State to, Event event, EventContext context) {
        // restarts the ConnectRetryTimer (with initial value)
        restartConnectRetryTimer();

        // initiates a TCP connection to the other BGP peer
        callbacks.connectRemotePeer();

        // continues to listen for a TCP connection that may be initiated by a remote BGP peer
        callbacks.listenForTCPConnection();

        // changes its state to Connect
        LOGGER.fine("Changing state from ACTIVE to CONNECT");
    }

    // @AsyncExecute
    public void onDelayOpenTimer_ExpiresActive(State from, State to, Event event, EventContext context) {
        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // stops and clears the DelayOpenTimer (set to zero)
        delayOpenTimer.cancel();
        delayOpenTime = 0;

        // completes the BGP initialization
        callbacks.completeBGPPeerInitialization();

        // sends the OPEN message to its remote peer
        callbacks.sendOpenMessage();

        // sets its hold timer to a large value, and
        restartHoldTimer(240);

        // changes its state to OpenSent
        LOGGER.fine("Changing state from ACTIVE to OPEN_SENT");
    }

    // @AsyncExecute
    public void onTcpConnection_ValidActive(State from, State to, Event event, EventContext context) {
        // the local system processes the TCP connection flags
        callbacks.processTCPConnection();

        // Remain in ACTIVE state
        LOGGER.fine("Remaining in ACTIVE state");
    }

    // @AsyncExecute
    public void onTcp_CR_InvalidActive(State from, State to, Event event, EventContext context) {
        // the local system rejects the TCP connection and
        callbacks.rejectTCPConnection();

        // stays in the Active State
        LOGGER.fine("Remaining in ACTIVE state");
    }

    // TODO: In response to the success of a TCP connection (Event 16 or Event
    //      17), the local system checks the DelayOpen optional attribute
    //      prior to processing.
    //        If the DelayOpen attribute is set to TRUE, the local system:
    //          - stops the ConnectRetryTimer and sets the ConnectRetryTimer
    //            to zero,
    //          - sets the DelayOpenTimer to the initial value
    //            (DelayOpenTime), and
    //          - stays in the Active state.
    //        If the DelayOpen attribute is set to FALSE, the local system:
    //          - sets the ConnectRetryTimer to zero,
    //          - completes the BGP initialization,
    //          - sends the OPEN message to its peer,
    //          - sets its HoldTimer to a large value, and
    //          - changes its state to OpenSent.
    //      A HoldTimer value of 4 minutes is suggested as a "large value" for
    //      the HoldTimer.

    // @AsyncExecute
    public void onTcpConnectionFailsActive(State from, State to, Event event, EventContext context) {
        // restarts the ConnectRetryTimer (with the initial value)
        restartConnectRetryTimer();

        // stops and clears the DelayOpenTimer (sets the value to zero)
        delayOpenTimer.cancel();
        delayOpenTime = 0;

        // releases all BGP resource
        callbacks.releaseBGPResources();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // optionally performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from ACTIVE to IDLE");
    }

    // @AsyncExecute
    public void onBGPOpen_with_DelayOpenTimer_runningActive(State from, State to, Event event, EventContext context) {
        // stops the ConnectRetryTimer (if running) and sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // stops and clears the DelayOpenTimer (sets to zero)
        delayOpenTimer.cancel();
        delayOpenTime = 0;

        // completes the BGP initialization
        callbacks.completeBGPPeerInitialization();

        // sends an OPEN message
        callbacks.sendOpenMessage();

        // sends a KEEPALIVE message
        callbacks.sendKeepaliveMessage();

        if (holdTime > 0 && holdTime < 90) {
            // starts the KeepaliveTimer to initial value
            restartKeepaliveTimer();

            // resets the HoldTimer to the negotiated value
            restartHoldTimer(90);
        } else {
            // resets the KeepaliveTimer (set to zero)
            keepaliveTimer.cancel();
            keepaliveTime = 0;

            // resets the HoldTimer to zero
            holdTimer.cancel();
            holdTime = 0;
        }

        // changes its state to OpenConfirm
        LOGGER.fine("Changing state from ACTIVE to OPEN_CONFIRM");
    }

    // @AsyncExecute
    public void onBGPHeaderErrActive(State from, State to, Event event, EventContext context) {
        // (optionally) sends a NOTIFICATION message with the appropriate error code if the SendNOTIFICATIONwithoutOPEN attribute is set to TRUE
        if (sendNOTIFICATIONwithoutOPEN) {
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_MESSAGE_HEADER_ERROR, (byte) 0, null);
        }

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from ACTIVE to IDLE");
    }

    // @AsyncExecute
    public void onBGPOpenMsgErrActive(State from, State to, Event event, EventContext context) {
        // (optionally) sends a NOTIFICATION message with the appropriate error code if the SendNOTIFICATIONwithoutOPEN attribute is set to TRUE
        if (sendNOTIFICATIONwithoutOPEN) {
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_OPEN_MESSAGE_ERROR, (byte) 0, null);
        }

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from ACTIVE to IDLE");
    }

    // @AsyncExecute
    public void onNotifMsgVerErrActive(State from, State to, Event event, EventContext context) {
        if (delayOpenTime > 0 && delayOpenTime < 120) {
            // stops the ConnectRetryTimer (if running) and sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // stops and resets the DelayOpenTimer (sets to zero)
            delayOpenTimer.cancel();
            delayOpenTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // changes its state to Idle
            LOGGER.fine("Changing state from ACTIVE to IDLE");
        } else {
            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE{
            if (dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from ACTIVE to IDLE");
        }
    }

    // @AsyncExecute
    public void onAnyOtherEventActive(State from, State to, Event event, EventContext context) {
        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by one
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from ACTIVE to IDLE");

    }
    // ------------------------------------------------------------------------------------------------------------

    // OPEN_SENT --------------------------------------------------------------------------------------------------
    // @AsyncExecute
    public void onManualStopOpenSent(State from, State to, Event event, EventContext context) {
        // sends the NOTIFICATION with a Cease
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE, (byte) 0, null);

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // sets the ConnectRetryCounter to zero
        connectRetryCounter = 0;

        // changes its state to Idle
        LOGGER.fine("Changing state from OPEN_SENT to IDLE");
    }

    // @AsyncExecute
    public void onAutomaticStopOpenSent(State from, State to, Event event, EventContext context) {
        // sends the NOTIFICATION with a Cease
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE, (byte) 0, null);

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all the BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from OPEN_SENT to IDLE");
    }

    // @AsyncExecute
    public void onHoldTimer_ExpiresOpenSent(State from, State to, Event event, EventContext context) {
        // sends a NOTIFICATION message with the error code Hold Timer Expired
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_HOLD_TIMER_EXPIRED, (byte) 0, null);

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from OPEN_SENT to IDLE");

    }

    // @AsyncExecute
    public void onTcpConnectionFailsOpenSent(State from, State to, Event event, EventContext context) {
        // closes the BGP connection
        callbacks.closeBGPConnection();

        // restarts the ConnectRetryTimer
        restartConnectRetryTimer();

        // continues to listen for a connection that may be initiated by the remote BGP peer
        callbacks.listenForTCPConnection();

        // changes its state to Active
        LOGGER.fine("Changing state from OPEN_SENT to ACTIVE");
    }

    // @AsyncExecute
    public void onBGPOpenOpenSent(State from, State to, Event event, EventContext context) {
        // resets the DelayOpenTimer to zero
        delayOpenTimer.cancel();
        delayOpenTime = 0;

        // sets the BGP ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // sends a KEEPALIVE message
        callbacks.sendKeepaliveMessage();

        // sets a KeepaliveTimer
        restartKeepaliveTimer();

        // sets the HoldTimer according to the negotiated value
        restartHoldTimer(90);

        // changes its state to OpenConfirm
        LOGGER.fine("Changing state from OPEN_SENT to OPEN_CONFIRM");
    }

    // @AsyncExecute
    public void onBGPHeaderErrOpenSent(State from, State to, Event event, EventContext context) {
        // sends a NOTIFICATION message with the appropriate error code
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_MESSAGE_HEADER_ERROR, (byte) 0, null);

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from OPEN_SENT to IDLE");
    }

    // @AsyncExecute
    public void onBGPOpenMsgErrOpenSent(State from, State to, Event event, EventContext context) {
        // sends a NOTIFICATION message with the appropriate error code
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_OPEN_MESSAGE_ERROR, (byte) 0, null);

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from OPEN_SENT to IDLE");
    }

    // @AsyncExecute
    public void onOpenCollisionDumpOpenSent(State from, State to, Event event, EventContext context) {
        // sends a NOTIFICATION with a Cease
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE, (byte) 0, null);

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from OPEN_SENT to IDLE");
    }

    // @AsyncExecute
    public void onNotifMsgVerErrOpenSent(State from, State to, Event event, EventContext context) {
        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // changes its state to Idle
        LOGGER.fine("Changing state from OPEN_SENT to IDLE");
    }

    // @AsyncExecute
    public void onAnyOtherEventOpenSent(State from, State to, Event event, EventContext context) {
        // sends the NOTIFICATION with the Error Code Finite State Machine Error
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_FINITE_STATE_MACHINE_ERROR, (byte) 0, null);

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from OPEN_SENT to IDLE");
    }
    // ------------------------------------------------------------------------------------------------------------

    // CONFIRM ----------------------------------------------------------------------------------------------------
    // @AsyncExecute
    public void onManualStopOpenConfirm(State from, State to, Event event, EventContext context) {
        // sends the NOTIFICATION message with a Cease
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE, (byte) 0, null);

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // sets the ConnectRetryCounter to zero
        connectRetryCounter = 0;

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // changes its state to Idle
        LOGGER.fine("Changing state from OPEN_CONFIRM to IDLE");
    }

    // @AsyncExecute
    public void onAutomaticStopOpenConfirm(State from, State to, Event event, EventContext context) {
        // sends the NOTIFICATION message with a Cease
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE, (byte) 0, null);

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from OPEN_CONFIRM to IDLE");
    }

    // @AsyncExecute
    public void onHoldTimer_ExpiresOpenConfirm(State from, State to, Event event, EventContext context) {
        // sends the NOTIFICATION message with the Error Code Hold Timer Expired
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_HOLD_TIMER_EXPIRED, (byte) 0, null);

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from OPEN_CONFIRM to IDLE");
    }

    // @AsyncExecute
    public void onKeepaliveTimer_ExpiresOpenConfirm(State from, State to, Event event, EventContext context) {
        // sends a KEEPALIVE message
        callbacks.sendKeepaliveMessage();

        // restarts the KeepaliveTimer
        restartKeepaliveTimer();

        // remains in the OpenConfirmed state
        LOGGER.fine("Remaining in the OPEN_CONFIRM state");
    }

    // @AsyncExecute
    public void onTcpConnectionFails_NotifMsgOpenConfirm(State from, State to, Event event, EventContext context) {
        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from OPEN_CONFIRM to IDLE");
    }

    // @AsyncExecute
    public void onNotifMsgVerErrOpenConfirm(State from, State to, Event event, EventContext context) {
        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // changes its state to Idle
        LOGGER.fine("Changing state from OPEN_CONFIRM to IDLE");
    }

    // @AsyncExecute
    public void onBGPHeaderErrOpenConfirm(State from, State to, Event event, EventContext context) {
        // sends a NOTIFICATION message with the appropriate error code
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_MESSAGE_HEADER_ERROR, (byte) 0, null);

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from OPEN_CONFIRM to IDLE");
    }

    // @AsyncExecute
    public void onBGPOpenMsgErrOpenConfirm(State from, State to, Event event, EventContext context) {
        // sends a NOTIFICATION message with the appropriate error code
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_OPEN_MESSAGE_ERROR, (byte) 0, null);

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from OPEN_CONFIRM to IDLE");
    }

    // @AsyncExecute
    public void onOpenCollisionDumpOpenConfirm(State from, State to, Event event, EventContext context) {
        // sends a NOTIFICATION with a Cease
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE, (byte) 0, null);

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from OPEN_CONFIRM to IDLE");
    }

    // @AsyncExecute
    public void onKeepAliveMsgOpenConfirm(State from, State to, Event event, EventContext context) {
        // restarts the HoldTimer
        restartHoldTimer(90);

        // changes its state to Established
        LOGGER.fine("Changing state from OPEN_CONFIRM to ESTABLISHED");
    }

    // @AsyncExecute
    public void onAnyOtherEventOpenConfirm(State from, State to, Event event, EventContext context) {
        // sends a NOTIFICATION with a code of Finite State Machine Error
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_FINITE_STATE_MACHINE_ERROR, (byte) 0, null);

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from OPEN_CONFIRM to IDLE");
    }
    // ------------------------------------------------------------------------------------------------------------

    // ESTABLISHED ------------------------------------------------------------------------------------------------
    // TODO: If the local system receives a valid OPEN message (BGPOpen (Event
    //      19)), the collision detect function is processed per Section 6.8.
    //      If this connection is to be dropped due to connection collision,
    //      the local system:
    //        - sends a NOTIFICATION with a Cease,
    //        - sets the ConnectRetryTimer to zero,
    //        - releases all BGP resources,
    //        - drops the TCP connection (send TCP FIN),
    //        - increments the ConnectRetryCounter by 1,
    //        - (optionally) performs peer oscillation damping if the
    //          DampPeerOscillations attribute is set to TRUE, and
    //        - changes its state to Idle.

    // @AsyncExecute
    public void onManualStopEstablished(State from, State to, Event event, EventContext context) {
        // sends the NOTIFICATION message with a Cease
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE, (byte) 0, null);

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // deletes all routes associated with this connection
        callbacks.deleteAllRoutes();

        // releases BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // sets the ConnectRetryCounter to zero
        connectRetryCounter = 0;

        // changes its state to Idle
        LOGGER.fine("Changing state from ESTABLISHED to IDLE");
    }

    // @AsyncExecute
    public void onAutomaticStopEstablished(State from, State to, Event event, EventContext context) {
        // sends the NOTIFICATION message with a Cease
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE, (byte) 0, null);

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // deletes all routes associated with this connection
        callbacks.deleteAllRoutes();

        // releases BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // sets the ConnectRetryCounter to zero
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        callbacks.performPeerOscillationDamping();

        // changes its state to Idle
        LOGGER.fine("Changing state from ESTABLISHED to IDLE");
    }

    // @AsyncExecute
    public void onHoldTimer_ExpiresEstablished(State from, State to, Event event, EventContext context) {
        // sends a NOTIFICATION message with the Error Code Hold Timer Expired
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_HOLD_TIMER_EXPIRED, (byte) 0, null);

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from ESTABLISHED to IDLE");
    }

    // @AsyncExecute
    public void onKeepaliveTimer_ExpiresEstablished(State from, State to, Event event, EventContext context) {
        // sends a KEEPALIVE message
        callbacks.sendKeepaliveMessage();

        // restarts its KeepaliveTimer, unless the negotiated HoldTime value is zero
        if (holdTime != 0) {
            restartKeepaliveTimer();
        }
    }

    // @AsyncExecute
    public void onOpenCollisionDumpEstablished(State from, State to, Event event, EventContext context) {
        // sends a NOTIFICATION with a Cease
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE, (byte) 0, null);

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // deletes all routes associated with this connection
        callbacks.deleteAllRoutes();

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from ESTABLISHED to IDLE");
    }

    // @AsyncExecute
    public void onTcpConnectionFails_NotifMsgVerErr_NotifMsgEstablished(State from, State to, Event event, EventContext context) {
        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // deletes all routes associated with this connection
        callbacks.deleteAllRoutes();

        // releases all the BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // changes its state to Idle
        LOGGER.fine("Changing state from ESTABLISHED to IDLE");
    }

    // @AsyncExecute
    public void onKeepAliveMsgEstablished(State from, State to, Event event, EventContext context) {
        // restarts its HoldTimer, if the negotiated HoldTime value is non-zero
        restartHoldTimer(90);

        // remains in the Established state
        LOGGER.fine("Remaining in ESTABLISHED state");
    }

    // @AsyncExecute
    public void onUpdateMsgEstablished(State from, State to, Event event, EventContext context) {
        // processes the message
        callbacks.processUpdateMessage();

        // restarts its HoldTimer, if the negotiated HoldTime value is non-zero
        if (holdTime != 0) {
            restartHoldTimer(90);
        }

        // remains in the Established state
        LOGGER.fine("Remaining in ESTABLISHED state");
    }

    // @AsyncExecute
    public void onUpdateMsgErrEstablished(State from, State to, Event event, EventContext context) {
        // sends a NOTIFICATION message with an Update error
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_UPDATE_MESSAGE_ERROR, (byte) 0, null);

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // deletes all routes associated with this connection
        callbacks.deleteAllRoutes();

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from ESTABLISHED to IDLE");
    }

    // @AsyncExecute
    public void onAnyOtherEventEstablished(State from, State to, Event event, EventContext context) {
        // sends a NOTIFICATION message with the Error Code Finite State Machine Error
        callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_FINITE_STATE_MACHINE_ERROR, (byte) 0, null);

        // deletes all routes associated with this connection
        callbacks.deleteAllRoutes();

        // sets the ConnectRetryTimer to zero
        connectRetryTimer.cancel();
        connectRetryTime = 0;

        // releases all BGP resources
        callbacks.releaseBGPResources();

        // drops the TCP connection+
        callbacks.dropTCPConnection();

        // increments the ConnectRetryCounter by 1
        connectRetryCounter++;

        // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
        if (dampPeerOscillations) {
            callbacks.performPeerOscillationDamping();
        }

        // changes its state to Idle
        LOGGER.fine("Changing state from ESTABLISHED to IDLE");
    }
    // ------------------------------------------------------------------------------------------------------------

    public void onIdle(State from, State to, Event event, EventContext context) {
        LOGGER.fine("Now the state is IDLE");
    }

    public void onConnect(State from, State to, Event event, EventContext context) {
        LOGGER.fine("Now the state is CONNECTED");
    }

    public void onActive(State from, State to, Event event, EventContext context) {
        LOGGER.fine("Now the state is ACTIVE");
    }

    public void onOpenSent(State from, State to, Event event, EventContext context) {
        LOGGER.fine("Now the state is OPEN_SENT");
    }

    public void onOpenConfirm(State from, State to, Event event, EventContext context) {
        LOGGER.fine("Now the state is OPEN_CONFIRM");
    }

    public void onEstablished(State from, State to, Event event, EventContext context) {
        LOGGER.fine("Now the state is ESTABLISHED");
    }
}
