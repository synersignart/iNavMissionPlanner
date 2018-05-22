// AUTO-GENERATED CODE: DO NOT EDIT!!!

package edu.wlu.cs.msppg;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.ByteArrayOutputStream;

public class Parser {

    private int state;
    private byte message_direction;
    private byte message_id;
    private byte message_length_expected;
    private byte message_length_received;
    private ByteArrayOutputStream message_buffer;
    private byte message_checksum;

    public Parser() {

        this.state = 0;
        this.message_buffer = new ByteArrayOutputStream();
    }

    private static ByteBuffer newByteBuffer(int capacity) {
        ByteBuffer bb = ByteBuffer.allocate(capacity);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb;
    }

   private static byte CRC8(byte [] data, int beg, int end) {

        int crc = 0x00;

        for (int k=beg; k<end; ++k) {

            int extract = (int)data[k] & 0xFF;

            crc ^= extract;
        }

        return (byte)crc;
    }

    private static byte CRC_dvb_s2(byte [] data, int beg, int end) {
        int crc = 0;
        for (int k=beg; k<end; ++k) {

            byte a = data[k];
            crc ^= a;
            for (int ii = 0; ii < 8; ++ii) {
                if ((crc & 0x80) > 0) {
                    crc = (crc << 1) ^ 0xD5;
                } else {
                    crc = crc << 1;
                }
            }
        }
        return (byte)crc;
    }

    public void parse(byte b) {

        switch (this.state) {

            case 0:               // sync char 1
                if (b == 36) { // $
                    this.state++;
                }
                break;        

            case 1:               // sync char 2
                if (b == 77) { // M
                    this.state++;
                }
                else {            // restart and try again
                    this.state = 0;
                }
                break;

            case 2:               // direction (should be >)
                if (b == 62) { // >
                    this.message_direction = 1;
                }
                else {            // <
                    this.message_direction = 0;
                }
                this.state++;
                break;

            case 3:
                this.message_length_expected = b;
                this.message_checksum = b;
                // setup arraybuffer
                this.message_length_received = 0;
                this.state++;
                break;

            case 4:
                this.message_id = b;
                this.message_checksum ^= b;
                this.message_buffer.reset();
                if (this.message_length_expected > 0) {
                    // process payload
                    this.state++;
                }
                else {
                    // no payload
                    this.state += 2;
                }
                break;

            case 5: // payload
                this.message_buffer.write(b);
                this.message_checksum ^= b;
                this.message_length_received++;
                if (this.message_length_received >= this.message_length_expected) {
                    this.state++;
                }
                break;

            case 6:
                this.state = 0;
                if (this.message_checksum == b) {

                    ByteBuffer bb = newByteBuffer(this.message_length_received);
                    bb.put(this.message_buffer.toByteArray(), 0, this.message_length_received);

                    switch (this.message_id) {
                        case (byte)101:
                            if (this.MSP_STATUS_handler != null) {
                                this.MSP_STATUS_handler.handle_MSP_STATUS(
                                bb.getShort(0),
                                bb.getShort(2),
                                bb.getShort(4),
                                bb.getInt(6),
                                bb.get(10));
                            }
                            break;

                        case (byte)150:
                            if (this.MSP_STATUS_EX_handler != null) {
                                this.MSP_STATUS_EX_handler.handle_MSP_STATUS_EX(
                                bb.getShort(0),
                                bb.getShort(2),
                                bb.getShort(4),
                                bb.getInt(6),
                                bb.get(10),
                                bb.getShort(11),
                                bb.getShort(13),
                                bb.get(15));
                            }
                            break;

                        case (byte)35:
                            if (this.MSP_SET_MODE_RANGE_handler != null) {
                                this.MSP_SET_MODE_RANGE_handler.handle_MSP_SET_MODE_RANGE(
                                bb.get(0),
                                bb.get(1),
                                bb.get(2),
                                bb.get(3),
                                bb.get(4));
                            }
                            break;

                        case (byte)105:
                            if (this.MSP_RC_handler != null) {
                                this.MSP_RC_handler.handle_MSP_RC(
                                bb.getShort(0),
                                bb.getShort(2),
                                bb.getShort(4),
                                bb.getShort(6),
                                bb.getShort(8),
                                bb.getShort(10),
                                bb.getShort(12));
                            }
                            break;

                        case (byte)73:
                            if (this.MSP_LOOP_TIME_handler != null) {
                                this.MSP_LOOP_TIME_handler.handle_MSP_LOOP_TIME(
                                bb.getShort(0));
                            }
                            break;

                        case (byte)58:
                            if (this.MSP_SONAR_ALTITUDE_handler != null) {
                                this.MSP_SONAR_ALTITUDE_handler.handle_MSP_SONAR_ALTITUDE(
                                bb.getInt(0));
                            }
                            break;

                        case (byte)106:
                            if (this.MSP_RAW_GPS_handler != null) {
                                this.MSP_RAW_GPS_handler.handle_MSP_RAW_GPS(
                                bb.get(0),
                                bb.get(1),
                                bb.getInt(2),
                                bb.getInt(6),
                                bb.getShort(10),
                                bb.getShort(12),
                                bb.getShort(14),
                                bb.getShort(16));
                            }
                            break;

                        case (byte)34:
                            if (this.MSP_MODE_RANGES_handler != null) {
                                this.MSP_MODE_RANGES_handler.handle_MSP_MODE_RANGES(
                                bb);
                            }
                            break;


                    }
                }
        }
    }

    private MSP_STATUS_Handler MSP_STATUS_handler;

    public void set_MSP_STATUS_Handler(MSP_STATUS_Handler handler) {

        this.MSP_STATUS_handler = handler;
    }

    public byte [] serialize_MSP_STATUS_Request() {


        byte [] message = new byte[6];

        message[0] = 36; // 0x24
        message[1] = 77; // 0x4D
        message[2] = 60; // 0x3C => Request
        message[3] = 0;
        message[4] = (byte)101;
        message[5] = (byte)101;

        return message;
    }

    public byte [] serialize_MSP_STATUS(short cycletime, short i2cErrorCount, short sensorStatus, int boxModeFlags, byte profile) {

        ByteBuffer bb = newByteBuffer(11);

        bb.putShort(cycletime);
        bb.putShort(i2cErrorCount);
        bb.putShort(sensorStatus);
        bb.putInt(boxModeFlags);
        bb.put(profile);

        byte [] message = new byte[20];
        message[0] = 36;
        message[1] = 88;
        message[2] = 60; // 0x3C => Request
        message[3] = 0;
        message[4] = (byte)101;
        message[5] = (byte)0;
        message[6] = 11;
        message[7] = 0;
        byte [] data = bb.array();
        int k;
        for (k=0; k<data.length; ++k) {
            message[k+8] = data[k];
        }

        message[19] = CRC_dvb_s2(message, 3, 19);

        return message;
    }

    private MSP_STATUS_EX_Handler MSP_STATUS_EX_handler;

    public void set_MSP_STATUS_EX_Handler(MSP_STATUS_EX_Handler handler) {

        this.MSP_STATUS_EX_handler = handler;
    }

    public byte [] serialize_MSP_STATUS_EX_Request() {


        byte [] message = new byte[6];

        message[0] = 36; // 0x24
        message[1] = 77; // 0x4D
        message[2] = 60; // 0x3C => Request
        message[3] = 0;
        message[4] = (byte)150;
        message[5] = (byte)150;

        return message;
    }

    public byte [] serialize_MSP_STATUS_EX(short cycletime, short i2cErrorCount, short sensorStatus, int boxModeFlags, byte profile, short systemload, short armingFlags, byte accCalibrationArmingFlags) {

        ByteBuffer bb = newByteBuffer(16);

        bb.putShort(cycletime);
        bb.putShort(i2cErrorCount);
        bb.putShort(sensorStatus);
        bb.putInt(boxModeFlags);
        bb.put(profile);
        bb.putShort(systemload);
        bb.putShort(armingFlags);
        bb.put(accCalibrationArmingFlags);

        byte [] message = new byte[25];
        message[0] = 36;
        message[1] = 88;
        message[2] = 60; // 0x3C => Request
        message[3] = 0;
        message[4] = (byte)150;
        message[5] = (byte)0;
        message[6] = 16;
        message[7] = 0;
        byte [] data = bb.array();
        int k;
        for (k=0; k<data.length; ++k) {
            message[k+8] = data[k];
        }

        message[24] = CRC_dvb_s2(message, 3, 24);

        return message;
    }

    private MSP_SET_MODE_RANGE_Handler MSP_SET_MODE_RANGE_handler;

    public void set_MSP_SET_MODE_RANGE_Handler(MSP_SET_MODE_RANGE_Handler handler) {

        this.MSP_SET_MODE_RANGE_handler = handler;
    }

    public byte [] serialize_MSP_SET_MODE_RANGE_Request() {


        byte [] message = new byte[6];

        message[0] = 36; // 0x24
        message[1] = 77; // 0x4D
        message[2] = 60; // 0x3C => Request
        message[3] = 0;
        message[4] = (byte)35;
        message[5] = (byte)35;

        return message;
    }

    public byte [] serialize_MSP_SET_MODE_RANGE(byte n, byte box, byte aux, byte start, byte end) {

        ByteBuffer bb = newByteBuffer(5);

        bb.put(n);
        bb.put(box);
        bb.put(aux);
        bb.put(start);
        bb.put(end);

        byte [] message = new byte[14];
        message[0] = 36;
        message[1] = 88;
        message[2] = 60; // 0x3C => Request
        message[3] = 0;
        message[4] = (byte)35;
        message[5] = (byte)0;
        message[6] = 5;
        message[7] = 0;
        byte [] data = bb.array();
        int k;
        for (k=0; k<data.length; ++k) {
            message[k+8] = data[k];
        }

        message[13] = CRC_dvb_s2(message, 3, 13);

        return message;
    }

    public byte [] serialize_MSP_EEPROM_WRITE() {

        ByteBuffer bb = newByteBuffer(0);


        byte [] message = new byte[9];
        message[0] = 36;
        message[1] = 88;
        message[2] = 60; // 0x3C => Request
        message[3] = 0;
        message[4] = (byte)250;
        message[5] = (byte)0;
        message[6] = 0;
        message[7] = 0;
        byte [] data = bb.array();
        int k;
        for (k=0; k<data.length; ++k) {
            message[k+8] = data[k];
        }

        message[8] = CRC_dvb_s2(message, 3, 8);

        return message;
    }

    private MSP_RC_Handler MSP_RC_handler;

    public void set_MSP_RC_Handler(MSP_RC_Handler handler) {

        this.MSP_RC_handler = handler;
    }

    public byte [] serialize_MSP_RC_Request() {


        byte [] message = new byte[6];

        message[0] = 36; // 0x24
        message[1] = 77; // 0x4D
        message[2] = 60; // 0x3C => Request
        message[3] = 0;
        message[4] = (byte)105;
        message[5] = (byte)105;

        return message;
    }

    public byte [] serialize_MSP_RC(short roll, short pitch, short yaw, short throttle, short aux1, short aux2, short aux3) {

        ByteBuffer bb = newByteBuffer(14);

        bb.putShort(roll);
        bb.putShort(pitch);
        bb.putShort(yaw);
        bb.putShort(throttle);
        bb.putShort(aux1);
        bb.putShort(aux2);
        bb.putShort(aux3);

        byte [] message = new byte[23];
        message[0] = 36;
        message[1] = 88;
        message[2] = 60; // 0x3C => Request
        message[3] = 0;
        message[4] = (byte)105;
        message[5] = (byte)0;
        message[6] = 14;
        message[7] = 0;
        byte [] data = bb.array();
        int k;
        for (k=0; k<data.length; ++k) {
            message[k+8] = data[k];
        }

        message[22] = CRC_dvb_s2(message, 3, 22);

        return message;
    }

    private MSP_LOOP_TIME_Handler MSP_LOOP_TIME_handler;

    public void set_MSP_LOOP_TIME_Handler(MSP_LOOP_TIME_Handler handler) {

        this.MSP_LOOP_TIME_handler = handler;
    }

    public byte [] serialize_MSP_LOOP_TIME_Request() {


        byte [] message = new byte[6];

        message[0] = 36; // 0x24
        message[1] = 77; // 0x4D
        message[2] = 60; // 0x3C => Request
        message[3] = 0;
        message[4] = (byte)73;
        message[5] = (byte)73;

        return message;
    }

    public byte [] serialize_MSP_LOOP_TIME(short cycletime) {

        ByteBuffer bb = newByteBuffer(2);

        bb.putShort(cycletime);

        byte [] message = new byte[11];
        message[0] = 36;
        message[1] = 88;
        message[2] = 60; // 0x3C => Request
        message[3] = 0;
        message[4] = (byte)73;
        message[5] = (byte)0;
        message[6] = 2;
        message[7] = 0;
        byte [] data = bb.array();
        int k;
        for (k=0; k<data.length; ++k) {
            message[k+8] = data[k];
        }

        message[10] = CRC_dvb_s2(message, 3, 10);

        return message;
    }

    private MSP_SONAR_ALTITUDE_Handler MSP_SONAR_ALTITUDE_handler;

    public void set_MSP_SONAR_ALTITUDE_Handler(MSP_SONAR_ALTITUDE_Handler handler) {

        this.MSP_SONAR_ALTITUDE_handler = handler;
    }

    public byte [] serialize_MSP_SONAR_ALTITUDE_Request() {


        byte [] message = new byte[6];

        message[0] = 36; // 0x24
        message[1] = 77; // 0x4D
        message[2] = 60; // 0x3C => Request
        message[3] = 0;
        message[4] = (byte)58;
        message[5] = (byte)58;

        return message;
    }

    public byte [] serialize_MSP_SONAR_ALTITUDE(int altitude) {

        ByteBuffer bb = newByteBuffer(4);

        bb.putInt(altitude);

        byte [] message = new byte[13];
        message[0] = 36;
        message[1] = 88;
        message[2] = 60; // 0x3C => Request
        message[3] = 0;
        message[4] = (byte)58;
        message[5] = (byte)0;
        message[6] = 4;
        message[7] = 0;
        byte [] data = bb.array();
        int k;
        for (k=0; k<data.length; ++k) {
            message[k+8] = data[k];
        }

        message[12] = CRC_dvb_s2(message, 3, 12);

        return message;
    }

    private MSP_RAW_GPS_Handler MSP_RAW_GPS_handler;

    public void set_MSP_RAW_GPS_Handler(MSP_RAW_GPS_Handler handler) {

        this.MSP_RAW_GPS_handler = handler;
    }

    public byte [] serialize_MSP_RAW_GPS_Request() {


        byte [] message = new byte[6];

        message[0] = 36; // 0x24
        message[1] = 77; // 0x4D
        message[2] = 60; // 0x3C => Request
        message[3] = 0;
        message[4] = (byte)106;
        message[5] = (byte)106;

        return message;
    }

    public byte [] serialize_MSP_RAW_GPS(byte fixType, byte numSat, int lat, int lon, short alt, short groundSpeed, short groundCourse, short hdop) {

        ByteBuffer bb = newByteBuffer(18);

        bb.put(fixType);
        bb.put(numSat);
        bb.putInt(lat);
        bb.putInt(lon);
        bb.putShort(alt);
        bb.putShort(groundSpeed);
        bb.putShort(groundCourse);
        bb.putShort(hdop);

        byte [] message = new byte[27];
        message[0] = 36;
        message[1] = 88;
        message[2] = 60; // 0x3C => Request
        message[3] = 0;
        message[4] = (byte)106;
        message[5] = (byte)0;
        message[6] = 18;
        message[7] = 0;
        byte [] data = bb.array();
        int k;
        for (k=0; k<data.length; ++k) {
            message[k+8] = data[k];
        }

        message[26] = CRC_dvb_s2(message, 3, 26);

        return message;
    }

    private MSP_MODE_RANGES_Handler MSP_MODE_RANGES_handler;

    public void set_MSP_MODE_RANGES_Handler(MSP_MODE_RANGES_Handler handler) {

        this.MSP_MODE_RANGES_handler = handler;
    }

    public byte [] serialize_MSP_MODE_RANGES_Request() {


        byte [] message = new byte[6];

        message[0] = 36; // 0x24
        message[1] = 77; // 0x4D
        message[2] = 60; // 0x3C => Request
        message[3] = 0;
        message[4] = (byte)34;
        message[5] = (byte)34;

        return message;
    }

    // not generated: public byte [] serialize_MSP_MODE_RANGES
}