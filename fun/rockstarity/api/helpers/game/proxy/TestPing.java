package fun.rockstarity.api.helpers.game.proxy;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.client.network.status.IClientStatusNetHandler;
import net.minecraft.network.NettyPacketDecoder;
import net.minecraft.network.NettyPacketEncoder;
import net.minecraft.network.NettyVarint21FrameDecoder;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraft.network.status.client.CPingPacket;
import net.minecraft.network.status.server.SPongPacket;
import net.minecraft.network.status.server.SServerInfoPacket;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class TestPing {
    public String state = "";

    private long pingSentAt;
    private NetworkManager pingDestination = null;
    private Proxy proxy;
    private static final ThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).build());

    public void run(String ip, int port, Proxy proxy) {
        this.proxy = proxy;
        TestPing.EXECUTOR.submit(() -> ping(ip, port));
    }

    private void ping(String ip, int port) {
        state = "Pinging " + ip + "...";
        NetworkManager clientConnection;
        try {
            clientConnection = createTestClientConnection(InetAddress.getByName(ip), port);
        } catch (UnknownHostException e) {
            state = TextFormatting.RED + "Can't connect to proxy";
            return;
        } catch (Exception e) {
            state = TextFormatting.RED + "Can't ping " + ip;
            return;
        }
        pingDestination = clientConnection;
        clientConnection.setNetHandler(new IClientStatusNetHandler() {
        private boolean successful;
            @Override
            public void handleServerInfo(SServerInfoPacket packetIn) {
                pingSentAt = Util.milliTime();
                clientConnection.sendPacket(new CPingPacket(pingSentAt));
            }

            @Override
            public void handlePong(SPongPacket packetIn) {
                successful = true;
                pingDestination = null;
                long pingToServer = Util.milliTime() - pingSentAt;
                state = "Ping: " + pingToServer + " ms";

                clientConnection.closeChannel(new TranslationTextComponent("multiplayer.status.finished"));
            }

            @Override
            public void onDisconnect(ITextComponent reason) {
                pingDestination = null;
                if (!successful) {
                    state = TextFormatting.RED + "Can't ping " + ip + ": " + reason.getString();
                    /*Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                        run(ip, port, proxy);
                    }, 5, TimeUnit.SECONDS);*/
                }
            }

            @Override
            public NetworkManager getNetworkManager() {
                return clientConnection;
            }
        });

        try {
            clientConnection.sendPacket(new CHandshakePacket(ip, port, ProtocolType.STATUS));
            pingSentAt = Util.milliTime();
            clientConnection.sendPacket(new CPingPacket(pingSentAt));
        } catch (Throwable throwable) {
            state = TextFormatting.RED + "Can't ping " + ip;
        }
    }

    private NetworkManager createTestClientConnection(InetAddress address, int port) {
        final NetworkManager clientConnection = new NetworkManager(PacketDirection.CLIENTBOUND);

        (new Bootstrap()).group(NetworkManager.CLIENT_NIO_EVENTLOOP.getValue()).handler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel) {
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException ignored) {
                }

                channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30))
                        .addLast("splitter", new NettyVarint21FrameDecoder())
                        .addLast("decoder", new NettyPacketDecoder(PacketDirection.CLIENTBOUND))
                        .addLast("prepender", new ProtobufVarint32LengthFieldPrepender())
                        .addLast("encoder", new NettyPacketEncoder(PacketDirection.SERVERBOUND))
                        .addLast("packet_handler", clientConnection);

                if (proxy.type == Proxy.ProxyType.SOCKS5) {
                    channel.pipeline().addFirst(new Socks5ProxyHandler(new InetSocketAddress(proxy.getIp(), proxy.getPort()), proxy.username.isEmpty() ? null : proxy.username, proxy.password.isEmpty() ? null : proxy.password));
                } else {
                    channel.pipeline().addFirst(new Socks4ProxyHandler(new InetSocketAddress(proxy.getIp(), proxy.getPort()), proxy.username.isEmpty() ? null : proxy.username));
                }
            }
        }).channel(NioSocketChannel.class).connect(address, port).syncUninterruptibly();
        return clientConnection;
    }

    public void pingPendingNetworks() {
        if (pingDestination != null) {
            if (pingDestination.isChannelOpen()) {
                pingDestination.tick();
            } else {
                //System.out.println("Р‘Р›РЇРўР¬: " + pingDestination.getChannel());
                if (pingDestination.getNetHandler() == null) {
                    return;
                }
                pingDestination.handleDisconnection();
            }
        }
    }
}
