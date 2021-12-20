/*
 * Copyright 2019-2021 CloudNetService team & contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dytanic.cloudnet.driver.network.netty.http;

import de.dytanic.cloudnet.driver.network.http.IHttpChannel;
import de.dytanic.cloudnet.driver.network.http.websocket.IWebSocketChannel;
import de.dytanic.cloudnet.driver.network.http.websocket.IWebSocketListener;
import de.dytanic.cloudnet.driver.network.http.websocket.WebSocketFrameType;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.NonNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
final class NettyWebSocketServerChannel implements IWebSocketChannel {

  private final List<IWebSocketListener> webSocketListeners = new CopyOnWriteArrayList<>();

  private final Channel channel;
  private final IHttpChannel httpChannel;

  public NettyWebSocketServerChannel(
    @NonNull IHttpChannel httpChannel,
    @NonNull Channel channel
  ) {
    this.httpChannel = httpChannel;
    this.channel = channel;
  }

  @Override
  public @NonNull IWebSocketChannel addListener(@NonNull IWebSocketListener... listeners) {
    this.webSocketListeners.addAll(Arrays.asList(listeners));
    return this;
  }

  @Override
  public @NonNull IWebSocketChannel removeListener(@NonNull IWebSocketListener... listeners) {
    this.webSocketListeners.removeIf(listener -> Arrays.asList(listeners).contains(listener));
    return this;
  }

  @Override
  public @NonNull IWebSocketChannel removeListener(@NonNull Collection<Class<? extends IWebSocketListener>> classes) {
    this.webSocketListeners.removeIf(listener -> classes.contains(listener.getClass()));
    return this;
  }

  @Override
  public @NonNull IWebSocketChannel removeListener(@NonNull ClassLoader classLoader) {
    this.webSocketListeners.removeIf(listener -> listener.getClass().getClassLoader().equals(classLoader));
    return this;
  }

  @Override
  public @NonNull IWebSocketChannel clearListeners() {
    this.webSocketListeners.clear();
    return this;
  }

  @Override
  public @NonNull Collection<IWebSocketListener> listeners() {
    return this.webSocketListeners;
  }

  @Override
  public @NonNull IWebSocketChannel sendWebSocketFrame(@NonNull WebSocketFrameType type, @NonNull String text) {
    return this.sendWebSocketFrame(type, text.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public @NonNull IWebSocketChannel sendWebSocketFrame(@NonNull WebSocketFrameType webSocketFrameType, byte[] bytes) {
    WebSocketFrame webSocketFrame = switch (webSocketFrameType) {
      case PING -> new PingWebSocketFrame(Unpooled.buffer(bytes.length).writeBytes(bytes));
      case PONG -> new PongWebSocketFrame(Unpooled.buffer(bytes.length).writeBytes(bytes));
      case TEXT -> new TextWebSocketFrame(Unpooled.buffer(bytes.length).writeBytes(bytes));
      default -> new BinaryWebSocketFrame(Unpooled.buffer(bytes.length).writeBytes(bytes));
    };

    this.channel.writeAndFlush(webSocketFrame).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    return this;
  }

  @Override
  public @NonNull IHttpChannel channel() {
    return this.httpChannel;
  }

  @Override
  public void close(int statusCode, @NonNull String reasonText) {
    var statusCodeReference = new AtomicInteger(statusCode);
    var reasonTextReference = new AtomicReference<>(reasonText);

    for (var listener : this.webSocketListeners) {
      listener.handleClose(this, statusCodeReference, reasonTextReference);
    }

    this.channel.writeAndFlush(new CloseWebSocketFrame(statusCodeReference.get(), reasonTextReference.get()))
      .addListener(ChannelFutureListener.CLOSE);
  }

  @Override
  public void close() {
    this.close(200, "default closing");
  }
}