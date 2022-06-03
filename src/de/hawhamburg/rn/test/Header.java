package de.hawhamburg.rn.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Header {
  private byte[] fullHeader;
  private byte[] ipReceiver, portReceiver, portSender, ipSender, msgType, checksum, payloadLenAsBytes;
  private int payloadLen;


  public Header(byte[] receivedHeader) {
    checkHeaderLength(receivedHeader);
    this.fullHeader = receivedHeader;
    getHeaderParts();
  }

  public Header(Socket receiver, int portSender, int msgType, byte[] payload) throws IOException {
    ipReceiver = receiver.getInetAddress().getAddress();
    portReceiver = Util.intToLowerTwoBytes(receiver.getPort());
    this.portSender = Util.intToLowerTwoBytes(portSender);
    ipSender = Inet4Address.getLocalHost().getAddress();
    this.msgType = Util.intToLowerTwoBytes(msgType);
    checksum = Util.intToLowerTwoBytes(0);
    payloadLen = payload.length;
    compose();
  }

  public Header(InetAddress addressReceiver, int portReceiver, int portSender, int msgType, byte[] payload) throws IOException {
    ipReceiver = addressReceiver.getAddress();
    this.portReceiver = Util.intToLowerTwoBytes(portReceiver);
    this.portSender = Util.intToLowerTwoBytes(portSender);
    ipSender = Inet4Address.getLocalHost().getAddress();
    this.msgType = Util.intToLowerTwoBytes(msgType);
    checksum = Util.intToLowerTwoBytes(0);
    payloadLen = payload.length;
    compose();
  }

  private void checkHeaderLength(byte[] receivedHeader) {
    if (receivedHeader.length != 20) {
      throw new IllegalArgumentException("Given header is not 20 bytes long.");
    }
  }

  private void getHeaderParts() {
    ipReceiver = Arrays.copyOfRange(fullHeader, 0, 4);
    portReceiver = Arrays.copyOfRange(fullHeader, 4, 6);
    portSender = Arrays.copyOfRange(fullHeader, 6, 8);
    ipSender = Arrays.copyOfRange(fullHeader, 8, 12);
    msgType = Arrays.copyOfRange(fullHeader, 12, 14);
    checksum = Arrays.copyOfRange(fullHeader, 14, 16);
    payloadLenAsBytes = Arrays.copyOfRange(fullHeader, 16, 20);
    payloadLen = Util.bytesToInt(payloadLenAsBytes);
  }

  public void compose() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    outputStream.write(ipReceiver);
    outputStream.write(portReceiver);
    outputStream.write(portSender);
    outputStream.write(ipSender);
    outputStream.write(msgType);
    outputStream.write(checksum);
    byte[] plLength = Util.intToBytes(payloadLen);
    outputStream.write(plLength);
    fullHeader = outputStream.toByteArray();
  }

  public byte[] getIpReceiver() {
    return ipReceiver;
  }

  public void setIpReceiver(byte[] ipReceiver) {
    this.ipReceiver = ipReceiver;
  }

  public byte[] getPortReceiver() {
    return portReceiver;
  }

  public void setPortReceiver(byte[] portReceiver) {
    this.portReceiver = portReceiver;
  }

  public byte[] getPortSender() {
    return portSender;
  }

  public void setPortSender(byte[] portSender) {
    this.portSender = portSender;
  }

  public byte[] getIpSender() {
    return ipSender;
  }

  public void setIpSender(byte[] ipSender) {
    this.ipSender = ipSender;
  }

  public byte[] getMsgType() {
    return msgType;
  }

  public void setMsgType(byte[] msgType) {
    this.msgType = msgType;
  }

  public byte[] getChecksum() {
    return checksum;
  }

  public void setChecksum(byte[] checksum) {
    this.checksum = checksum;
  }

  public byte[] getPayloadLenAsBytes() {
    return payloadLenAsBytes;
  }

  public int getPayloadLen() {
    return payloadLen;
  }

  public void setPayloadLenAsBytes(byte[] payloadLenAsBytes) {
    this.payloadLenAsBytes = payloadLenAsBytes;
  }

  public void setPayloadLen(int payloadLen) {
    this.payloadLen = payloadLen;
  }

  public byte[] getFullHeader() {
    return fullHeader;
  }

  public void setFullHeader(byte[] fullHeader) {
    this.fullHeader = fullHeader;
  }
}
