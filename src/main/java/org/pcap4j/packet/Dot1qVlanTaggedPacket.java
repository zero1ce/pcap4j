/*_##########################################################################
  _##
  _##  Copyright (C) 2012  Kaito Yamada
  _##
  _##########################################################################
*/

package org.pcap4j.packet;

import static org.pcap4j.util.ByteArrays.SHORT_SIZE_IN_BYTE;
import java.util.ArrayList;
import java.util.List;
import org.pcap4j.packet.namedvalue.EtherType;
import org.pcap4j.util.ByteArrays;

/**
 * @author Kaito Yamada
 * @since pcap4j 0.9.5
 */
public class Dot1qVlanTaggedPacket extends AbstractPacket {

  /**
   *
   */
  private static final long serialVersionUID = 1522789079803339400L;

  private final Dot1qVlanTagHeader header;
  private final Packet payload;

  /**
   *
   * @param rawData
   * @return
   */
  public static Dot1qVlanTaggedPacket newPacket(byte[] rawData) {
    return new Dot1qVlanTaggedPacket(rawData);
  }

  private Dot1qVlanTaggedPacket(byte[] rawData) {
    this.header = new Dot1qVlanTagHeader(rawData);

    byte[] rawPayload
      = ByteArrays.getSubArray(
          rawData,
          this.header.length(),
          rawData.length - this.header.length()
        );

    this.payload
      = PacketFactory
          .newPacketByEtherType(rawPayload, header.getType().value());
  }

  private Dot1qVlanTaggedPacket(Builder builder) {
    if (
         builder == null
      || builder.type == null
      || builder.payloadBuilder == null
    ) {
      throw new NullPointerException();
    }

    this.payload = builder.payloadBuilder.build();
    this.header = new Dot1qVlanTagHeader(builder);
  }

  @Override
  public Dot1qVlanTagHeader getHeader() {
    return header;
  }

  @Override
  public Packet getPayload() {
    return payload;
  }

  @Override
  public Builder getBuilder() {
    return new Builder(this);
  }

  /**
   * @author Kaito Yamada
   * @since pcap4j 0.9.5
   */
  public final class Builder implements Packet.Builder {

    private EtherType type;
    private byte priority = 0;
    private byte cfi = 0;
    private short vid;
    private Packet.Builder payloadBuilder;

    /**
     *
     */
    public Builder() {}

    private Builder(Dot1qVlanTaggedPacket packet) {
      this.type = packet.header.type;
      this.priority = packet.header.priority;
      this.cfi = packet.header.cfi;
      this.vid = packet.header.vid;
      this.payloadBuilder = packet.payload.getBuilder();
    }

    /**
     *
     * @param type
     * @return
     */
    public Builder type(EtherType type) {
      this.type = type;
      return this;
    }

    /**
     *
     * @param priority
     * @return
     */
    public Builder priority(byte priority) {
      this.priority = priority;
      return this;
    }

    /**
     *
     * @param cfi
     * @return
     */
    public Builder cfi(byte cfi) {
      this.cfi = cfi;
      return this;
    }

    /**
     *
     * @param vid
     * @return
     */
    public Builder vid(short vid) {
      this.vid = vid;
      return this;
    }

    public Dot1qVlanTaggedPacket build() {
      return new Dot1qVlanTaggedPacket(this);
    }

  }

  public final class Dot1qVlanTagHeader extends AbstractHeader {

    /**
     *
     */
    private static final long serialVersionUID = 7130569411806479522L;

    private static final int PRIORITY_OFFSET = 0;
    private static final int PRIORITY_SIZE = 3;
    private static final int CFI_OFFSET = PRIORITY_OFFSET + PRIORITY_SIZE;
    private static final int CFI_SIZE = 1;
    private static final int VID_OFFSET = CFI_OFFSET + CFI_SIZE;
    private static final int VID_SIZE = 12;
    private static final int TYPE_OFFSET = VID_OFFSET + VID_SIZE;
    private static final int TYPE_SIZE = SHORT_SIZE_IN_BYTE;
    private static final int DOT1Q_TAG_HEADER_SIZE = TYPE_OFFSET + TYPE_SIZE;

    private final byte priority;
    private final byte cfi;
    private final short vid;
    private final EtherType type;

    private Dot1qVlanTagHeader(byte[] rawData) {
      if (rawData.length < DOT1Q_TAG_HEADER_SIZE) {
        throw new IllegalArgumentException();
      }

      short priorityAndCfiAndVid
        = ByteArrays.getShort(rawData, PRIORITY_OFFSET);

      this.priority = (byte)((priorityAndCfiAndVid & 0xE000) >> 13);
      this.cfi = (byte)((priorityAndCfiAndVid & 0x1000) >> 12);
      this.vid = (byte)(priorityAndCfiAndVid & 0x0FFF);
      this.type
        = EtherType.getInstance(ByteArrays.getShort(rawData, TYPE_OFFSET));
    }

    private Dot1qVlanTagHeader(Builder builder) {
      this.priority = builder.priority;
      this.cfi = builder.cfi;
      this.vid = builder.vid;
      this.type = builder.type;
    }

    /**
     *
     * @return
     */
    public byte getPriority() {
      return priority;
    }

    /**
     *
     * @return
     */
    public byte getCfi() {
      return cfi;
    }

    /**
     *
     * @return
     */
    public short getVid() {
      return vid;
    }

    /**
     *
     * @return
     */
    public EtherType getType() {
      return type;
    }

    @Override
    protected List<byte[]> getRawFields() {
      List<byte[]> rawFields = new ArrayList<byte[]>();
      rawFields.add(ByteArrays.toByteArray(type.value()));
      rawFields.add(
        ByteArrays.toByteArray(
          (short)((priority << 13) | (cfi << 12) | vid)
        )
      );
      return rawFields;
    }

    @Override
    public boolean isValid() { return true; }

    @Override
    public int length() {
      return DOT1Q_TAG_HEADER_SIZE;
    }

    @Override
    protected String buildString() {
      StringBuilder sb = new StringBuilder();

      sb.append("[IEEE802.1Q Tag header (")
        .append(length())
        .append(" bytes)]\n");
      sb.append("  Priority: ")
        .append(priority)
        .append("\n");
      sb.append("  CFI: ")
        .append(cfi)
        .append("\n");
      sb.append("  VID: ")
        .append(vid)
        .append("\n");
      sb.append("  Type: ")
        .append(type)
        .append("\n");

      return sb.toString();
    }

  }

}