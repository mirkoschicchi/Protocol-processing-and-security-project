package fi.utu.protproc.group3.finitestatemachine;

import fi.utu.protproc.group3.protocols.bgp4.BGP4MessageNotification;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.AsyncExecute;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class FSMImpl {
    public static final short DEFAULT_HOLD_TIME = 90;
    private static final Logger LOGGER = Logger.getLogger(FSMImpl.class.getName());

    public enum FSMEvent {
        ManualStart, ManualStop, AutomaticStart, ManualStart_with_PassiveTcpEstablishment,
        AutomaticStart_with_PassiveTcpEstablishment, AutomaticStart_with_DampPeerOscillations,
        AutomaticStart_with_DampPeerOscillations_and_PassiveTcpEstablishment,
        AutomaticStop, ConnectRetryTimer_Expires, HoldTimer_Expires, KeepaliveTimer_Expires,
        DelayOpenTimer_Expires, IdleHoldTimer_Expires, TcpConnection_Valid, Tcp_CR_Invalid,
        Tcp_CR_Acked, TcpConnectionConfirmed, TcpConnectionFails, BGPOpen,
        BGPOpen_with_DelayOpenTimer_running, BGPHeaderErr, BGPOpenMsgErr, OpenCollisionDump,
        NotifMsgVerErr, NotifMsg, KeepAliveMsg, UpdateMsg, UpdateMsgErr
    };

    public UntypedStateMachineBuilder builder;

    private final static FSMImpl builderInstance = new FSMImpl();
    public static UntypedStateMachine newInstance(InternalFSMCallbacks callbacks) {
        synchronized (builderInstance) {
            return builderInstance.builder.newStateMachine("IDLE", callbacks);
        }
    }

    private FSMImpl() {
        this.builder = StateMachineBuilderFactory.create(StateMachineSample.class, new Class<?>[] { InternalFSMCallbacks.class });
        // IDLE
        {
            builder.onEntry("IDLE").callMethod("onIdle");
            builder.externalTransition().from("IDLE").to("CONNECT").on(FSMEvent.ManualStart).callMethod("onManualStart_AutomaticStartIdle");
            builder.externalTransition().from("IDLE").to("CONNECT").on(FSMEvent.AutomaticStart).callMethod("onManualStart_AutomaticStartIdle");
            builder.externalTransition().from("IDLE").to("ACTIVE").on(FSMEvent.ManualStart_with_PassiveTcpEstablishment).callMethod("onManualStart_with_PassiveTcpEstablishment_AutomaticStart_with_PassiveTcpEstablishmentIdle");
            builder.externalTransition().from("IDLE").to("ACTIVE").on(FSMEvent.AutomaticStart_with_PassiveTcpEstablishment).callMethod("onManualStart_with_PassiveTcpEstablishment_AutomaticStart_with_PassiveTcpEstablishmentIdle");
        }

        // CONNECT
        {
            builder.onEntry("CONNECT").callMethod("onConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.ManualStop).callMethod("onManualStopConnect");
            builder.internalTransition().within("CONNECT").on(FSMEvent.ConnectRetryTimer_Expires).callMethod("onConnectRetryTimer_ExpiresConnect");
            builder.externalTransition().from("CONNECT").to("OPEN_SENT").on(FSMEvent.DelayOpenTimer_Expires).callMethod("onDelayOpenTimer_ExpiresConnect");
            builder.externalTransition().from("CONNECT").to("OPEN_SENT").on(FSMEvent.Tcp_CR_Acked).callMethod("onTcp_CR_Acked_TcpConnectionConfirmedConnect");
            builder.externalTransition().from("CONNECT").to("OPEN_SENT").on(FSMEvent.TcpConnectionConfirmed).callMethod("onTcp_CR_Acked_TcpConnectionConfirmedConnect");

            builder.externalTransition().from("CONNECT").to("OPEN_CONFIRM").on(FSMEvent.BGPOpen_with_DelayOpenTimer_running).callMethod("onBGPOpen_with_DelayOpenTimer_runningConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.BGPHeaderErr).callMethod("onBGPHeaderErrConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.BGPOpenMsgErr).callMethod("onBGPOpenMsgErrConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.NotifMsgVerErr).callMethod("onNotifMsgVerErrConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.AutomaticStop).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.HoldTimer_Expires).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.KeepaliveTimer_Expires).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.IdleHoldTimer_Expires).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.BGPOpen).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.OpenCollisionDump).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.NotifMsg).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.KeepAliveMsg).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.UpdateMsg).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.UpdateMsgErr).callMethod("onAnyOtherEventConnect");
        }

        // ACTIVE
        {
            builder.onEntry("ACTIVE").callMethod("onActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.ManualStop).callMethod("onManualStopActive");
            builder.externalTransition().from("ACTIVE").to("CONNECT").on(FSMEvent.ConnectRetryTimer_Expires).callMethod("onConnectRetryTimer_ExpiresActive");
            builder.externalTransition().from("ACTIVE").to("OPEN_SENT").on(FSMEvent.DelayOpenTimer_Expires).callMethod("onDelayOpenTimer_ExpiresActive");
            builder.internalTransition().within("ACTIVE").on(FSMEvent.TcpConnection_Valid).callMethod("onTcpConnection_ValidActive");
            builder.internalTransition().within("ACTIVE").on(FSMEvent.Tcp_CR_Invalid).callMethod("onTcp_CR_InvalidActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.TcpConnectionFails).callMethod("onTcpConnectionFailsActive");
            builder.externalTransition().from("ACTIVE").to("OPEN_CONFIRM").on(FSMEvent.BGPOpen_with_DelayOpenTimer_running).callMethod("onBGPOpen_with_DelayOpenTimer_runningActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.BGPHeaderErr).callMethod("onBGPHeaderErrActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.BGPOpenMsgErr).callMethod("onBGPOpenMsgErrActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.NotifMsgVerErr).callMethod("onNotifMsgVerErrActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.AutomaticStop).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.HoldTimer_Expires).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.KeepaliveTimer_Expires).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.IdleHoldTimer_Expires).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.BGPOpen).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.OpenCollisionDump).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.NotifMsg).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.KeepAliveMsg).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.UpdateMsg).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.UpdateMsgErr).callMethod("onAnyOtherEventActive");
        }

        // OPEN_SENT
        {
            builder.onEntry("OPEN_SENT").callMethod("onOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.ManualStop).callMethod("onManualStopOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.AutomaticStop).callMethod("onAutomaticStopOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.HoldTimer_Expires).callMethod("onHoldTimer_ExpiresOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("ACTIVE").on(FSMEvent.TcpConnectionFails).callMethod("onTcpConnectionFailsOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("OPEN_CONFIRM").on(FSMEvent.BGPOpen).callMethod("onBGPOpenOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.BGPHeaderErr).callMethod("onBGPHeaderErrOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.BGPOpenMsgErr).callMethod("onBGPOpenMsgErrOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.OpenCollisionDump).callMethod("onOpenCollisionDumpOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.NotifMsgVerErr).callMethod("onNotifMsgVerErrOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.ConnectRetryTimer_Expires).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.KeepaliveTimer_Expires).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.DelayOpenTimer_Expires).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.IdleHoldTimer_Expires).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.BGPOpen_with_DelayOpenTimer_running).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.NotifMsg).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.KeepAliveMsg).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.UpdateMsg).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.UpdateMsgErr).callMethod("onAnyOtherEventOpenSent");
        }

        // OPEN_CONFIRM
        {
            builder.onEntry("OPEN_CONFIRM").callMethod("onOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.ManualStop).callMethod("onManualStopOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.AutomaticStop).callMethod("onAutomaticStopOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.HoldTimer_Expires).callMethod("onHoldTimer_ExpiresOpenConfirm");
            builder.internalTransition().within("OPEN_CONFIRM").on(FSMEvent.KeepaliveTimer_Expires).callMethod("onKeepaliveTimer_ExpiresOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.TcpConnectionFails).callMethod("onTcpConnectionFails_NotifMsgOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.NotifMsg).callMethod("onTcpConnectionFails_NotifMsgOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.NotifMsgVerErr).callMethod("onNotifMsgVerErrOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.BGPHeaderErr).callMethod("onBGPHeaderErrOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.BGPOpenMsgErr).callMethod("onBGPOpenMsgErrOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.OpenCollisionDump).callMethod("onOpenCollisionDumpOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("ESTABLISHED").on(FSMEvent.KeepAliveMsg).callMethod("onKeepAliveMsgOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.ConnectRetryTimer_Expires).callMethod("onAnyOtherEventOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.DelayOpenTimer_Expires).callMethod("onAnyOtherEventOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.IdleHoldTimer_Expires).callMethod("onAnyOtherEventOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.BGPOpen_with_DelayOpenTimer_running).callMethod("onAnyOtherEventOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.UpdateMsg).callMethod("onAnyOtherEventOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.UpdateMsgErr).callMethod("onAnyOtherEventOpenConfirm");
        }

        // ESTABLISHED
        {
            builder.onEntry("ESTABLISHED").callMethod("onEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.ManualStop).callMethod("onManualStopEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.AutomaticStop).callMethod("onAutomaticStopEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.HoldTimer_Expires).callMethod("onHoldTimer_ExpiresEstablished");
            builder.internalTransition().within("ESTABLISHED").on(FSMEvent.KeepaliveTimer_Expires).callMethod("onKeepaliveTimer_ExpiresEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.OpenCollisionDump).callMethod("onOpenCollisionDumpEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.TcpConnectionFails).callMethod("onTcpConnectionFails_NotifMsgVerErr_NotifMsgEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.NotifMsgVerErr).callMethod("onTcpConnectionFails_NotifMsgVerErr_NotifMsgEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.NotifMsg).callMethod("onTcpConnectionFails_NotifMsgVerErr_NotifMsgEstablished");
            builder.internalTransition().within("ESTABLISHED").on(FSMEvent.KeepAliveMsg).callMethod("onKeepAliveMsgEstablished");
            builder.internalTransition().within("ESTABLISHED").on(FSMEvent.UpdateMsg).callMethod("onUpdateMsgEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.UpdateMsgErr).callMethod("onUpdateMsgErrEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.ConnectRetryTimer_Expires).callMethod("onAnyOtherEventEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.DelayOpenTimer_Expires).callMethod("onAnyOtherEventEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.IdleHoldTimer_Expires).callMethod("onAnyOtherEventEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.BGPOpen_with_DelayOpenTimer_running).callMethod("onAnyOtherEventEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.BGPHeaderErr).callMethod("onAnyOtherEventEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.BGPOpenMsgErr).callMethod("onAnyOtherEventEstablished");
        }
    }

    @StateMachineParameters(stateType=String.class, eventType=FSMEvent.class, contextType=BGPEventContext.class)
    static class StateMachineSample extends AbstractUntypedStateMachine {
        int connectRetryCounter = 0;
        int connectRetryTime = 120;
        int holdTime = DEFAULT_HOLD_TIME;
        int keepaliveTime = 30;

        Timer connectRetryTimer = new Timer();
        Timer holdTimer = new Timer();
        Timer keepaliveTimer = new Timer();

        // Optional attributes
        int delayOpenTime = 120; // Not sure
        Timer delayOpenTimer = new Timer();
        boolean delayOpen = true;
        boolean dampPeerOscillations = false;
        boolean sendNOTIFICATIONwithoutOPEN = false;

        private final InternalFSMCallbacks callbacks;

        public StateMachineSample(InternalFSMCallbacks callbacks) {
            Objects.requireNonNull(callbacks);

            this.callbacks = callbacks;
        }

        void restartConnectRetryTimer() {
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
                        fire(FSMEvent.ConnectRetryTimer_Expires);
                    }
                }
            };

            connectRetryTimer.schedule(connectRetryTimerTask, 0, 1000);
        }

        void restartDelayOpenTimer() {
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
                        fire(FSMEvent.DelayOpenTimer_Expires);
                    }
                }
            };

            delayOpenTimer.schedule(delayOpenTimerTask, 0, 1000);
        }

        void restartHoldTimer(int time) {
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
                        fire(FSMEvent.HoldTimer_Expires);
                    }
                }
            };

            holdTimer.schedule(holdTimerTask, 0, 1000);
        }

        void restartKeepaliveTimer() {
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
                        fire(FSMEvent.KeepaliveTimer_Expires);
                    }
                }
            };

            keepaliveTimer.schedule(keepaliveTimerTask, 0, 1000);
        }

        // IDLE -------------------------------------------------------------------------------------------------------
        @AsyncExecute
        protected void onManualStart_AutomaticStartIdle(String from, String to, FSMEvent event, BGPEventContext context) {
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

        @AsyncExecute
        protected void onManualStart_with_PassiveTcpEstablishment_AutomaticStart_with_PassiveTcpEstablishmentIdle(String from, String to, FSMEvent event, BGPEventContext context) {
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
        @AsyncExecute
        protected void onManualStopConnect(String from, String to, FSMEvent event, BGPEventContext context) {
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

        @AsyncExecute
        protected void onConnectRetryTimer_ExpiresConnect(String from, String to, FSMEvent event, BGPEventContext context) {
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

        @AsyncExecute
        protected void onDelayOpenTimer_ExpiresConnect(String from, String to, FSMEvent event, BGPEventContext context) {
            // sends an OPEN message to its peer
            callbacks.sendOpenMessage();

            // sets the HoldTimer to a large value
            restartHoldTimer(240);

            // changes its state to OpenSent
            LOGGER.fine("Changing state from CONNECT to OPEN_SENT");
        }

        @AsyncExecute
        public void onTcp_CR_Acked_TcpConnectionConfirmed_DelayOpenConnect(String from, String to, FSMEvent event, BGPEventContext context) {
            // stops the ConnectRetryTimer (if running) and sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // sets the DelayOpenTimer to the initial value
            restartDelayOpenTimer();

            // stays in the Connect state
            LOGGER.fine("Remaining in CONNECT state");
        }

        @AsyncExecute
        public void onTcp_CR_Acked_TcpConnectionConfirmedConnect(String from, String to, FSMEvent event, BGPEventContext context) {
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

        @AsyncExecute
        protected void onBGPOpen_with_DelayOpenTimer_runningConnect(String from, String to, FSMEvent event, BGPEventContext context) {
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

            if(holdTime != 0) {
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

        @AsyncExecute
        protected void onBGPHeaderErrConnect(String from, String to, FSMEvent event, BGPEventContext context) {
            // (optionally) If the SendNOTIFICATIONwithoutOPEN attribute is
            //          set to TRUE, then the local system first sends a NOTIFICATION
            //          message with the appropriate error code
            if(sendNOTIFICATIONwithoutOPEN) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from CONNECT to IDLE");
        }

        @AsyncExecute
        protected void onBGPOpenMsgErrConnect(String from, String to, FSMEvent event, BGPEventContext context) {
            // (optionally) If the SendNOTIFICATIONwithoutOPEN attribute is
            //          set to TRUE, then the local system first sends a NOTIFICATION
            //          message with the appropriate error code
            if(sendNOTIFICATIONwithoutOPEN) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from CONNECT to IDLE");
        }

        @AsyncExecute
        protected void onNotifMsgVerErrConnect(String from, String to, FSMEvent event, BGPEventContext context) {
            if(delayOpenTime > 0) {
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
                if(dampPeerOscillations) {
                    callbacks.performPeerOscillationDamping();
                }

                // changes its state to Idle
                LOGGER.fine("Changing state from CONNECT to IDLE");
            }
        }

        @AsyncExecute
        protected void onAnyOtherEventConnect(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from CONNECT to IDLE");
        }
        // ------------------------------------------------------------------------------------------------------------

        // ACTIVE -----------------------------------------------------------------------------------------------------
        @AsyncExecute
        protected void onManualStopActive(String from, String to, FSMEvent event, BGPEventContext context) {
            // If the DelayOpenTimer is running and the
            // SendNOTIFICATIONwithoutOPEN session attribute is set, the
            // local system sends a NOTIFICATION with a Cease
            if(delayOpenTime > 0 && delayOpenTime < 120 && sendNOTIFICATIONwithoutOPEN) {
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

        @AsyncExecute
        protected void onConnectRetryTimer_ExpiresActive(String from, String to, FSMEvent event, BGPEventContext context) {
            // restarts the ConnectRetryTimer (with initial value)
            restartConnectRetryTimer();

            // initiates a TCP connection to the other BGP peer
            callbacks.connectRemotePeer();

            // continues to listen for a TCP connection that may be initiated by a remote BGP peer
            callbacks.listenForTCPConnection();

            // changes its state to Connect
            LOGGER.fine("Changing state from ACTIVE to CONNECT");
        }

        @AsyncExecute
        protected void onDelayOpenTimer_ExpiresActive(String from, String to, FSMEvent event, BGPEventContext context) {
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

        @AsyncExecute
        protected void onTcpConnection_ValidActive(String from, String to, FSMEvent event, BGPEventContext context) {
            // the local system processes the TCP connection flags
            callbacks.processTCPConnection();

            // Remain in ACTIVE state
            LOGGER.fine("Remaining in ACTIVE state");
        }

        @AsyncExecute
        protected void onTcp_CR_InvalidActive(String from, String to, FSMEvent event, BGPEventContext context) {
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

        @AsyncExecute
        protected void onTcpConnectionFailsActive(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from ACTIVE to IDLE");
        }

        @AsyncExecute
        protected void onBGPOpen_with_DelayOpenTimer_runningActive(String from, String to, FSMEvent event, BGPEventContext context) {
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

            if(holdTime > 0 && holdTime < 90) {
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

        @AsyncExecute
        protected void onBGPHeaderErrActive(String from, String to, FSMEvent event, BGPEventContext context) {
            // (optionally) sends a NOTIFICATION message with the appropriate error code if the SendNOTIFICATIONwithoutOPEN attribute is set to TRUE
            if(sendNOTIFICATIONwithoutOPEN) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from ACTIVE to IDLE");
        }

        @AsyncExecute
        protected void onBGPOpenMsgErrActive(String from, String to, FSMEvent event, BGPEventContext context) {
            // (optionally) sends a NOTIFICATION message with the appropriate error code if the SendNOTIFICATIONwithoutOPEN attribute is set to TRUE
            if(sendNOTIFICATIONwithoutOPEN) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from ACTIVE to IDLE");
        }

        @AsyncExecute
        protected void onNotifMsgVerErrActive(String from, String to, FSMEvent event, BGPEventContext context) {
            if(delayOpenTime > 0 && delayOpenTime < 120) {
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
                if(dampPeerOscillations) {
                    callbacks.performPeerOscillationDamping();
                }

                // changes its state to Idle
                LOGGER.fine("Changing state from ACTIVE to IDLE");
            }
        }

        @AsyncExecute
        protected void onAnyOtherEventActive(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from ACTIVE to IDLE");

        }
        // ------------------------------------------------------------------------------------------------------------

        // OPEN_SENT --------------------------------------------------------------------------------------------------
        @AsyncExecute
        protected void onManualStopOpenSent(String from, String to, FSMEvent event, BGPEventContext context) {
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

        @AsyncExecute
        protected void onAutomaticStopOpenSent(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from OPEN_SENT to IDLE");
        }

        @AsyncExecute
        protected void onHoldTimer_ExpiresOpenSent(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from OPEN_SENT to IDLE");

        }

        @AsyncExecute
        protected void onTcpConnectionFailsOpenSent(String from, String to, FSMEvent event, BGPEventContext context) {
            // closes the BGP connection
            callbacks.closeBGPConnection();

            // restarts the ConnectRetryTimer
            restartConnectRetryTimer();

            // continues to listen for a connection that may be initiated by the remote BGP peer
            callbacks.listenForTCPConnection();

            // changes its state to Active
            LOGGER.fine("Changing state from OPEN_SENT to ACTIVE");
        }

        @AsyncExecute
        protected void onBGPOpenOpenSent(String from, String to, FSMEvent event, BGPEventContext context) {
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

        @AsyncExecute
        protected void onBGPHeaderErrOpenSent(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from OPEN_SENT to IDLE");
        }

        @AsyncExecute
        protected void onBGPOpenMsgErrOpenSent(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from OPEN_SENT to IDLE");
        }

        @AsyncExecute
        protected void onOpenCollisionDumpOpenSent(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from OPEN_SENT to IDLE");
        }

        @AsyncExecute
        protected void onNotifMsgVerErrOpenSent(String from, String to, FSMEvent event, BGPEventContext context) {
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

        @AsyncExecute
        protected void onAnyOtherEventOpenSent(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from OPEN_SENT to IDLE");
        }
        // ------------------------------------------------------------------------------------------------------------

        // CONFIRM ----------------------------------------------------------------------------------------------------
        @AsyncExecute
        protected void onManualStopOpenConfirm(String from, String to, FSMEvent event, BGPEventContext context) {
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

        @AsyncExecute
        protected void onAutomaticStopOpenConfirm(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from OPEN_CONFIRM to IDLE");
        }

        @AsyncExecute
        protected void onHoldTimer_ExpiresOpenConfirm(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from OPEN_CONFIRM to IDLE");
        }

        @AsyncExecute
        protected void onKeepaliveTimer_ExpiresOpenConfirm(String from, String to, FSMEvent event, BGPEventContext context) {
            // sends a KEEPALIVE message
            callbacks.sendKeepaliveMessage();

            // restarts the KeepaliveTimer
            restartKeepaliveTimer();

            // remains in the OpenConfirmed state
            LOGGER.fine("Remaining in the OPEN_CONFIRM state");
        }

        @AsyncExecute
        protected void onTcpConnectionFails_NotifMsgOpenConfirm(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from OPEN_CONFIRM to IDLE");
        }

        @AsyncExecute
        protected void onNotifMsgVerErrOpenConfirm(String from, String to, FSMEvent event, BGPEventContext context) {
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

        @AsyncExecute
        protected void onBGPHeaderErrOpenConfirm(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from OPEN_CONFIRM to IDLE");
        }

        @AsyncExecute
        protected void onBGPOpenMsgErrOpenConfirm(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from OPEN_CONFIRM to IDLE");
        }

        @AsyncExecute
        protected void onOpenCollisionDumpOpenConfirm(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from OPEN_CONFIRM to IDLE");
        }

        @AsyncExecute
        protected void onKeepAliveMsgOpenConfirm(String from, String to, FSMEvent event, BGPEventContext context) {
            // restarts the HoldTimer
            restartHoldTimer(90);

            // changes its state to Established
            LOGGER.fine("Changing state from OPEN_CONFIRM to ESTABLISHED");
        }

        @AsyncExecute
        protected void onAnyOtherEventOpenConfirm(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
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

        @AsyncExecute
        protected void onManualStopEstablished(String from, String to, FSMEvent event, BGPEventContext context) {
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

        @AsyncExecute
        protected void onAutomaticStopEstablished(String from, String to, FSMEvent event, BGPEventContext context) {
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

        @AsyncExecute
        protected void onHoldTimer_ExpiresEstablished(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from ESTABLISHED to IDLE");
        }

        @AsyncExecute
        protected void onKeepaliveTimer_ExpiresEstablished(String from, String to, FSMEvent event, BGPEventContext context) {
            // sends a KEEPALIVE message
            callbacks.sendKeepaliveMessage();

            // restarts its KeepaliveTimer, unless the negotiated HoldTime value is zero
            if(holdTime != 0) {
                restartKeepaliveTimer();
            }
        }

        @AsyncExecute
        protected void onOpenCollisionDumpEstablished(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from ESTABLISHED to IDLE");
        }

        @AsyncExecute
        protected void onTcpConnectionFails_NotifMsgVerErr_NotifMsgEstablished(String from, String to, FSMEvent event, BGPEventContext context) {
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

        @AsyncExecute
        protected void onKeepAliveMsgEstablished(String from, String to, FSMEvent event, BGPEventContext context) {
            // restarts its HoldTimer, if the negotiated HoldTime value is non-zero
            restartHoldTimer(90);

            // remains in the Established state
            LOGGER.fine("Remaining in ESTABLISHED state");
        }

        @AsyncExecute
        protected void onUpdateMsgEstablished(String from, String to, FSMEvent event, BGPEventContext context) {
            // processes the message
            callbacks.processUpdateMessage();

            // restarts its HoldTimer, if the negotiated HoldTime value is non-zero
            if(holdTime != 0) {
                restartHoldTimer(90);
            }

            // remains in the Established state
            LOGGER.fine("Remaining in ESTABLISHED state");
        }

        @AsyncExecute
        protected void onUpdateMsgErrEstablished(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from ESTABLISHED to IDLE");
        }

        @AsyncExecute
        protected void onAnyOtherEventEstablished(String from, String to, FSMEvent event, BGPEventContext context) {
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
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            LOGGER.fine("Changing state from ESTABLISHED to IDLE");
        }
        // ------------------------------------------------------------------------------------------------------------

        protected void onIdle(String from, String to, FSMEvent event, BGPEventContext context) {
            LOGGER.fine("Now the state is IDLE");
        }

        protected void onConnect(String from, String to, FSMEvent event, BGPEventContext context) {
            LOGGER.fine("Now the state is CONNECTED");
        }

        protected void onActive(String from, String to, FSMEvent event, BGPEventContext context) {
            LOGGER.fine("Now the state is ACTIVE");
        }

        protected void onOpenSent(String from, String to, FSMEvent event, BGPEventContext context) {
            LOGGER.fine("Now the state is OPEN_SENT");
        }

        protected void onOpenConfirm(String from, String to, FSMEvent event, BGPEventContext context) {
            LOGGER.fine("Now the state is OPEN_CONFIRM");
        }

        protected void onEstablished(String from, String to, FSMEvent event, BGPEventContext context) {
            LOGGER.fine("Now the state is ESTABLISHED");
        }
    }
}
