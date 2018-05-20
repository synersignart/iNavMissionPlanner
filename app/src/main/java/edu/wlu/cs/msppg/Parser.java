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

                        case (byte)121:
                            if (this.RC_NORMAL_handler != null) {
                                this.RC_NORMAL_handler.handle_RC_NORMAL(
                                bb.getFloat(0),
                                bb.getFloat(4),
                                bb.getFloat(8),
                                bb.getFloat(12),
                                bb.getFloat(16),
                                bb.getFloat(20),
                                bb.getFloat(24),
                                bb.getFloat(28));
                            }
                            break;

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

                        case (byte)122:
                            if (this.ATTITUDE_RADIANS_handler != null) {
                                this.ATTITUDE_RADIANS_handler.handle_ATTITUDE_RADIANS(
                                bb.getFloat(0),
                                bb.getFloat(4),
                                bb.getFloat(8));
                            }
                            break;


                    }
                }
        }
    }

    public byte [] serialize_SET_ARMED(byte flag) {

        ByteBuffer bb = newByteBuffer(1);

        bb.put(flag);

        byte [] message = new byte[7];
        message[0] = 36;
        message[1] = 77;
        message[2] = 60;
        message[3] = 1;
        message[4] = (byte)216;
        byte [] data = bb.array();
        int k;
        for (k=0; k<data.length; ++k) {
            message[k+5] = data[k];
        }

        message[6] = CRC8(message, 3, 5);

        return message;
    }

    private MSP_STATUS_EX_Handler MSP_STATUS_EX_handler;

    public void set_MSP_STATUS_EX_Handler(MSP_STATUS_EX_Handler handler) {

        this.MSP_STATUS_EX_handler = handler;
    }

    public byte [] serialize_MSP_STATUS_EX_Request() {


        byte [] message = new byte[6];

        message[0] = 36;
        message[1] = 77;
        message[2] = 60;
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

        byte [] message = new byte[22];
        message[0] = 36;
        message[1] = 77;
        message[2] = 62;
        message[3] = 16;
        message[4] = (byte)150;
        byte [] data = bb.array();
        int k;
        for (k=0; k<data.length; ++k) {
            message[k+5] = data[k];
        }

        message[21] = CRC8(message, 3, 20);

        return message;
    }

    private RC_NORMAL_Handler RC_NORMAL_handler;

    public void set_RC_NORMAL_Handler(RC_NORMAL_Handler handler) {

        this.RC_NORMAL_handler = handler;
    }

    public byte [] serialize_RC_NORMAL_Request() {


        byte [] message = new byte[6];

        message[0] = 36;
        message[1] = 77;
        message[2] = 60;
        message[3] = 0;
        message[4] = (byte)121;
        message[5] = (byte)121;

        return message;
    }

    public byte [] serialize_RC_NORMAL(float c1, float c2, float c3, float c4, float c5, float c6, float c7, float c8) {

        ByteBuffer bb = newByteBuffer(32);

        bb.putFloat(c1);
        bb.putFloat(c2);
        bb.putFloat(c3);
        bb.putFloat(c4);
        bb.putFloat(c5);
        bb.putFloat(c6);
        bb.putFloat(c7);
        bb.putFloat(c8);

        byte [] message = new byte[38];
        message[0] = 36;
        message[1] = 77;
        message[2] = 62;
        message[3] = 32;
        message[4] = (byte)121;
        byte [] data = bb.array();
        int k;
        for (k=0; k<data.length; ++k) {
            message[k+5] = data[k];
        }

        message[37] = CRC8(message, 3, 36);

        return message;
    }

    private MSP_STATUS_Handler MSP_STATUS_handler;

    public void set_MSP_STATUS_Handler(MSP_STATUS_Handler handler) {

        this.MSP_STATUS_handler = handler;
    }

    public byte [] serialize_MSP_STATUS_Request() {


        byte [] message = new byte[6];

        message[0] = 36;
        message[1] = 77;
        message[2] = 60;
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

        byte [] message = new byte[17];
        message[0] = 36;
        message[1] = 77;
        message[2] = 62;
        message[3] = 11;
        message[4] = (byte)101;
        byte [] data = bb.array();
        int k;
        for (k=0; k<data.length; ++k) {
            message[k+5] = data[k];
        }

        message[16] = CRC8(message, 3, 15);

        return message;
    }

    private MSP_LOOP_TIME_Handler MSP_LOOP_TIME_handler;

    public void set_MSP_LOOP_TIME_Handler(MSP_LOOP_TIME_Handler handler) {

        this.MSP_LOOP_TIME_handler = handler;
    }

    public byte [] serialize_MSP_LOOP_TIME_Request() {


        byte [] message = new byte[6];

        message[0] = 36;
        message[1] = 77;
        message[2] = 60;
        message[3] = 0;
        message[4] = (byte)73;
        message[5] = (byte)73;

        return message;
    }

    public byte [] serialize_MSP_LOOP_TIME(short cycletime) {

        ByteBuffer bb = newByteBuffer(2);

        bb.putShort(cycletime);

        byte [] message = new byte[8];
        message[0] = 36;
        message[1] = 77;
        message[2] = 62;
        message[3] = 2;
        message[4] = (byte)73;
        byte [] data = bb.array();
        int k;
        for (k=0; k<data.length; ++k) {
            message[k+5] = data[k];
        }

        message[7] = CRC8(message, 3, 6);

        return message;
    }

    private MSP_SONAR_ALTITUDE_Handler MSP_SONAR_ALTITUDE_handler;

    public void set_MSP_SONAR_ALTITUDE_Handler(MSP_SONAR_ALTITUDE_Handler handler) {

        this.MSP_SONAR_ALTITUDE_handler = handler;
    }

    public byte [] serialize_MSP_SONAR_ALTITUDE_Request() {


        byte [] message = new byte[6];

        message[0] = 36;
        message[1] = 77;
        message[2] = 60;
        message[3] = 0;
        message[4] = (byte)58;
        message[5] = (byte)58;

        return message;
    }

    public byte [] serialize_MSP_SONAR_ALTITUDE(int altitude) {

        ByteBuffer bb = newByteBuffer(4);

        bb.putInt(altitude);

        byte [] message = new byte[10];
        message[0] = 36;
        message[1] = 77;
        message[2] = 62;
        message[3] = 4;
        message[4] = (byte)58;
        byte [] data = bb.array();
        int k;
        for (k=0; k<data.length; ++k) {
            message[k+5] = data[k];
        }

        message[9] = CRC8(message, 3, 8);

        return message;
    }

    private MSP_RAW_GPS_Handler MSP_RAW_GPS_handler;

    public void set_MSP_RAW_GPS_Handler(MSP_RAW_GPS_Handler handler) {

        this.MSP_RAW_GPS_handler = handler;
    }

    public byte [] serialize_MSP_RAW_GPS_Request() {


        byte [] message = new byte[6];

        message[0] = 36;
        message[1] = 77;
        message[2] = 60;
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

        byte [] message = new byte[24];
        message[0] = 36;
        message[1] = 77;
        message[2] = 62;
        message[3] = 18;
        message[4] = (byte)106;
        byte [] data = bb.array();
        int k;
        for (k=0; k<data.length; ++k) {
            message[k+5] = data[k];
        }

        message[23] = CRC8(message, 3, 22);

        return message;
    }

    private ATTITUDE_RADIANS_Handler ATTITUDE_RADIANS_handler;

    public void set_ATTITUDE_RADIANS_Handler(ATTITUDE_RADIANS_Handler handler) {

        this.ATTITUDE_RADIANS_handler = handler;
    }

    public byte [] serialize_ATTITUDE_RADIANS_Request() {


        byte [] message = new byte[6];

        message[0] = 36;
        message[1] = 77;
        message[2] = 60;
        message[3] = 0;
        message[4] = (byte)122;
        message[5] = (byte)122;

        return message;
    }

    public byte [] serialize_ATTITUDE_RADIANS(float roll, float pitch, float yaw) {

        ByteBuffer bb = newByteBuffer(12);

        bb.putFloat(roll);
        bb.putFloat(pitch);
        bb.putFloat(yaw);

        byte [] message = new byte[18];
        message[0] = 36;
        message[1] = 77;
        message[2] = 62;
        message[3] = 12;
        message[4] = (byte)122;
        byte [] data = bb.array();
        int k;
        for (k=0; k<data.length; ++k) {
            message[k+5] = data[k];
        }

        message[17] = CRC8(message, 3, 16);

        return message;
    }

    public byte [] serialize_SET_MOTOR_NORMAL(float m1, float m2, float m3, float m4) {

        ByteBuffer bb = newByteBuffer(16);

        bb.putFloat(m1);
        bb.putFloat(m2);
        bb.putFloat(m3);
        bb.putFloat(m4);

        byte [] message = new byte[22];
        message[0] = 36;
        message[1] = 77;
        message[2] = 60;
        message[3] = 16;
        message[4] = (byte)215;
        byte [] data = bb.array();
        int k;
        for (k=0; k<data.length; ++k) {
            message[k+5] = data[k];
        }

        message[21] = CRC8(message, 3, 20);

        return message;
    }

}