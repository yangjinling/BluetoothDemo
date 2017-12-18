package com.cw.bluetoothdemo.util;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by leonn on 2017/5/31.
 */

public class BJCWUtil {
    private final static int INIT = 0x0000;
    private final static int POLINOMIAL = 0x1021;

    private static int calc_crc(int crc, int ch) {
        int i;
        if (ch < 0) {
            ch = 256 + ch;
        }
        ch <<= 8;
        for (i = 8; i > 0; i--) {
            int n = (ch ^ crc) & 0x8000;
            if (n != 0) {
                crc = (crc << 1) ^ POLINOMIAL;
            } else {
                crc <<= 1;
            }
            ch <<= 1;
        }
        return crc;
    }

	/*
     * Generate GetCRC 天毅发卡箱计算CRC函数
	 *
	 * @param p[] 要计算数据 nLen 计算数据的长度
	 *
	 * @return返回一个CRC值
	 */

    public static int GetCRC(byte p[], int nLen) {
        byte ch;
        int i;
        int crc = INIT;

        for (i = 0; i < nLen; i++) {
            ch = p[i];
            crc = calc_crc(crc, (int) ch);
        }
        return crc;
    }

    //

    private final static int TABLE_CRC16_R[] = {0x0000, 0x1189, 0x2312, 0x329B, 0x4624, 0x57AD, 0x6536, 0x74BF, 0x8C48, 0x9DC1, 0xAF5A, 0xBED3, 0xCA6C, 0xDBE5, 0xE97E, 0xF8F7, 0x1081, 0x0108, 0x3393, 0x221A, 0x56A5, 0x472C, 0x75B7, 0x643E, 0x9CC9, 0x8D40, 0xBFDB, 0xAE52, 0xDAED, 0xCB64, 0xF9FF, 0xE876, 0x2102, 0x308B, 0x0210, 0x1399, 0x6726, 0x76AF, 0x4434, 0x55BD, 0xAD4A, 0xBCC3, 0x8E58, 0x9FD1, 0xEB6E, 0xFAE7, 0xC87C, 0xD9F5, 0x3183, 0x200A, 0x1291, 0x0318, 0x77A7, 0x662E, 0x54B5, 0x453C, 0xBDCB, 0xAC42, 0x9ED9, 0x8F50, 0xFBEF, 0xEA66, 0xD8FD, 0xC974,

            0x4204, 0x538D, 0x6116, 0x709F, 0x0420, 0x15A9, 0x2732, 0x36BB, 0xCE4C, 0xDFC5, 0xED5E, 0xFCD7, 0x8868, 0x99E1, 0xAB7A, 0xBAF3, 0x5285, 0x430C, 0x7197, 0x601E, 0x14A1, 0x0528, 0x37B3, 0x263A, 0xDECD, 0xCF44, 0xFDDF, 0xEC56, 0x98E9, 0x8960, 0xBBFB, 0xAA72, 0x6306, 0x728F, 0x4014, 0x519D, 0x2522, 0x34AB, 0x0630, 0x17B9, 0xEF4E, 0xFEC7, 0xCC5C, 0xDDD5, 0xA96A, 0xB8E3, 0x8A78, 0x9BF1, 0x7387, 0x620E, 0x5095, 0x411C, 0x35A3, 0x242A, 0x16B1, 0x0738, 0xFFCF, 0xEE46, 0xDCDD, 0xCD54, 0xB9EB, 0xA862, 0x9AF9, 0x8B70,

            0x8408, 0x9581, 0xA71A, 0xB693, 0xC22C, 0xD3A5, 0xE13E, 0xF0B7, 0x0840, 0x19C9, 0x2B52, 0x3ADB, 0x4E64, 0x5FED, 0x6D76, 0x7CFF, 0x9489, 0x8500, 0xB79B, 0xA612, 0xD2AD, 0xC324, 0xF1BF, 0xE036, 0x18C1, 0x0948, 0x3BD3, 0x2A5A, 0x5EE5, 0x4F6C, 0x7DF7, 0x6C7E, 0xA50A, 0xB483, 0x8618, 0x9791, 0xE32E, 0xF2A7, 0xC03C, 0xD1B5, 0x2942, 0x38CB, 0x0A50, 0x1BD9, 0x6F66, 0x7EEF, 0x4C74, 0x5DFD, 0xB58B, 0xA402, 0x9699, 0x8710, 0xF3AF, 0xE226, 0xD0BD, 0xC134, 0x39C3, 0x284A, 0x1AD1, 0x0B58, 0x7FE7, 0x6E6E, 0x5CF5, 0x4D7C,

            0xC60C, 0xD785, 0xE51E, 0xF497, 0x8028, 0x91A1, 0xA33A, 0xB2B3, 0x4A44, 0x5BCD, 0x6956, 0x78DF, 0x0C60, 0x1DE9, 0x2F72, 0x3EFB, 0xD68D, 0xC704, 0xF59F, 0xE416, 0x90A9, 0x8120, 0xB3BB, 0xA232, 0x5AC5, 0x4B4C, 0x79D7, 0x685E, 0x1CE1, 0x0D68, 0x3FF3, 0x2E7A, 0xE70E, 0xF687, 0xC41C, 0xD595, 0xA12A, 0xB0A3, 0x8238, 0x93B1, 0x6B46, 0x7ACF, 0x4854, 0x59DD, 0x2D62, 0x3CEB, 0x0E70, 0x1FF9, 0xF78F, 0xE606, 0xD49D, 0xC514, 0xB1AB, 0xA022, 0x92B9, 0x8330, 0x7BC7, 0x6A4E, 0x58D5, 0x495C, 0x3DE3, 0x2C6A, 0x1EF1, 0x0F78};

    /*
     * Cal_CRC16_Byte 天毅二代CRC16算法
     *
     * @param ptr[] 要计算数据 nLen 计算数据的长度
     *
     * @return返回一个CRC值
     */
    public static int Cal_CRC16_Byte(byte ptr[], int len) {
        int cData;
        int k, iCRC;
        int i = 0;
        iCRC = 0xFFFF;

        while (len != 0) {
            len--;
            cData = iCRC & 0xFF; // CRC 的低8位
            iCRC /= 256; // 高24位 右移8位
            cData ^= (int) ptr[i]; // 低8位和当前字节异或
            if (cData < 0) {
                cData = 256 + cData;
            }
            i++; // 指向下一字节
            k = TABLE_CRC16_R[cData]; // 查表求其CRC值
            iCRC ^= k; // 与查表值异或
        }

        return iCRC;
    }

    public static String track2 = "";
    public static String track3 = "";
    public static String strCardNum = "";
    public static String strtrack2 = "";

    public static boolean SplitCard(String strDate) {
        boolean bRet = SplitMargneticStripeCard(strDate);
        if (bRet) {
            OutputLog("解析数据成功");
            bRet = SplitCardDate(track2);
            if (bRet) {
                OutputLog("解析卡号成功");
                byte ptrack2[] = new byte[track2.length() / 2];
                int nTempLen = track2.length() / 2 - 2;
                byte pTemp[] = new byte[nTempLen];
                HexToAsc(ptrack2, track2.getBytes(), track2.getBytes().length - 2);
                System.arraycopy(ptrack2, 1, pTemp, 0, nTempLen);
                strtrack2 = new String(pTemp);
                OutputLog("strtrack2：" + strtrack2);
                byte pDst[] = new byte[strCardNum.length() / 2];
                HexToAsc(pDst, strCardNum.getBytes(), strCardNum.getBytes().length);
                strCardNum = new String(pDst);
                OutputLog("strCardNum：" + strCardNum);
                bRet = isNumeric(strCardNum);
                if (bRet) {
                    OutputLog("卡号：" + strCardNum);
                    return true;
                } else {
                    OutputLog("卡号不是纯数字");
                }
            } else {
                OutputLog("解析卡号失败");
            }
        } else {
            OutputLog("解析数据失败");
        }
        return false;
    }

    // 判断是否是纯数字
    private static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    private static boolean SplitMargneticStripeCard(String strDate) {
        int n3BIndex = strDate.indexOf("3B");
        int n3FIndex = strDate.indexOf("3F");
        int nDateLen = 0;
        if (strDate.length() == 0) {
            OutputLog("无数据");
            return false;
        }
        if (n3BIndex == -1) {
            OutputLog("无3B");
            return false;
        }
        if (n3BIndex != 0) {
            OutputLog("3B不是数据的头");
            return false;
        }
        if (n3FIndex == -1) {
            OutputLog("无3F");
            return false;
        }
        String strLog = String.format("3B位置:%d,3F位置:%d", n3BIndex, n3FIndex);
        OutputLog(strLog);
        nDateLen += n3FIndex;
        nDateLen += 2;
        track2 = strDate.substring(0, nDateLen);
        strLog = String.format("track2:%s", track2);
        OutputLog(strLog);

        int n3B3939Index = strDate.substring(nDateLen).indexOf("3B3939");
        if (n3B3939Index == -1) {
            track3 = "";
            return true;
        }
        n3FIndex = strDate.substring(nDateLen).indexOf("3F");
        if (strDate.length() != (nDateLen + n3FIndex + 2)) {
            OutputLog("3B3939返回的数据不正确");
            return true;
        }
        track3 = strDate.substring(nDateLen);
        strLog = String.format("track3:%s", track3);
        OutputLog(strLog);
        return true;
    }

    private static boolean SplitCardDate(String strDate) {
        int nCardNumIndex = strDate.indexOf("3D");
        if (strDate.length() == 0) {
            OutputLog("无数据");
            return false;
        }
        if (nCardNumIndex == -1) {
            OutputLog("无3D标识");
            return false;
        }
        strCardNum = strDate.substring(2, nCardNumIndex);
        String strLog = String.format("strCardNum:%s", strCardNum);
        OutputLog(strLog);
        return true;
    }

    public static void Memcpy(byte[] output, byte[] input, int outpos, int inpos, int len) {
        int i;
        for (i = 0; i < len; i++)
            output[outpos + i] = input[inpos + i];
    }

    public static int HexToAsc(byte[] pDst, byte[] pSrc, int nSrcLen) {
        int n = 0, m = 0;
        for (int i = 0; i < nSrcLen; i += 2) {
            // 输出高4位
            if (pSrc[m] >= '0' && pSrc[m] <= '9') {
                pDst[n] = (byte) ((pSrc[m] - '0') << 4);
            } else if (pSrc[n] >= 'A' && pSrc[n] <= 'F') {
                pDst[n] = (byte) ((pSrc[m] - 'A' + 10) << 4);
            } else {
                pDst[n] = (byte) ((pSrc[m] - 'a' + 10) << 4);
            }

            m++;

            // 输出低4位
            if (pSrc[m] >= '0' && pSrc[m] <= '9') {
                pDst[n] |= pSrc[m] - '0';
            } else if (pSrc[m] >= 'A' && pSrc[m] <= 'F') {
                pDst[n] |= pSrc[m] - 'A' + 10;
            } else {
                pDst[n] |= pSrc[m] - 'a' + 10;
            }

            n++;
            m++;
        }
        // 返回目标数据长度
        return nSrcLen / 2;
    }

    public static int AscToHex(char[] pDst, byte[] pSrc, int SrcLen) {
        String tab = "0123456789ABCDEF"; // 0x0-0xf的字符查找表
        int n = 0, m = 0;
        for (int i = 0; i < SrcLen; i++) {
            pDst[n++] = (char) tab.charAt((char) ((pSrc[m] >> 4) & 0x0f)); // 输出低4位
            pDst[n++] = (char) tab.charAt(pSrc[m] & 0x0f); // 输出高4位
            m++;
        }

        // 输出字符串加个结束符
        // pDst[n] = '\0';

        // 返回目标字符串长度
        return SrcLen * 2;
    }

    public static int IsOutTime() {
        int nCountTime = 0;
        int nMinute = 0;
        int nSecond = 0;
        Date dt = new Date();
        String strDt = new String();
        String strMinute = new String();
        String strSecond = new String();
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");

        strDt = sdfDate.format(dt);
        // 分
        strMinute = strDt.substring(10, 12);
        // 秒
        strSecond = strDt.substring(12, 14);

        nMinute = Integer.parseInt(strMinute);
        nSecond = Integer.parseInt(strSecond);

        nCountTime = nMinute * 60 + nSecond;
        return nCountTime;
    }

    public static final boolean bOpenLog = false;

    private static String getStringDateShort() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    public static void OutputLog(String string) {
        // TODO Auto-generated method stub
        // return;
        if (bOpenLog) {
            return;
        }
        // File file = new File("/sdcard/Card_LOG.txt");
        // BufferedWriter writer = null;
        // try {
        // writer = new BufferedWriter(new FileWriter(file, true));
        // if (writer != null) {
        // writer.write(getStringDateShort() + string + "\r\n");
        // writer.close();
        // }
        // } catch (IOException e) {
        // e.printStackTrace();
        // } finally {
        // if (writer != null) {
        // try {
        // writer.close();
        // } catch (IOException e1) {
        // }
        // }
        // }
        // Log.v("CGenUtil", string);
        Log.i("Cashway", string);
    }

    public static String pad80(String s, int len) {
        String data = "80000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        data = s + data;
        return data.substring(0, len);
    }

    public static String check80(String s) {
        for (int i = s.length() - 2; i >= 0; i -= 2) {

            String strData = s.substring(i, i + 2);
            // System.out.print(i);
            if (strData.endsWith("80")) {
                return s.substring(0, i);
            } else {
                if (strData.equals("00")) {
                    continue;
                }
                if (strData.equals("80")) {
                    return s.substring(0, i);
                } else {
                    return s;
                }
            }
        }
        return s;
    }

    // ///////// HexTostr,将“0x3A，0x58，0x67”变成“3A5876”
    public static String HexTostr(byte[] hex, int hexlen) {
        String str = "";
        if (hex.length == 0)
            return str;
        for (int i = 0; i < hexlen; i++) {
            String buf = Integer.toString(hex[i] & 0xFF, 16).toUpperCase();// &
            // 0xFF去掉负号
            if (buf.length() != 2)// 补位
            {
                str += "0" + buf;
            } else
                str += buf;
        }
        return str;
    }

    // //////// StrToHex,将形如“3a5867”转化成“0x3A，0x58，0x67”
    // 注意传入str之前需先将小写转成大写
    public static byte[] StrToHex(String str) {
        if (str.length() % 2 != 0)
            return null;
        byte[] ret = new byte[str.length() / 2];
        StringBuffer buf = new StringBuffer(2);
        int j = 0;
        for (int i = 0; i < str.length(); i++, j++) {
            buf.insert(0, str.charAt(i));
            buf.insert(1, str.charAt(i + 1));
            int t = Integer.parseInt(buf.toString(), 16);
            ret[j] = (byte) t;
            i++;
            buf.delete(0, 2);
        }
        return ret;
    }

    public static String HexTostr(char[] hex, int hexlen) {
        String str = "";
        if (hex.length == 0)
            return str;
        for (int i = 0; i < hexlen; i++) {
            String buf = Integer.toString(hex[i] & 0xFF, 16);// & 0xFF去掉负号
            if (buf.length() != 2)// 补位
            {
                str += "0" + buf;
            } else
                str += buf;
        }
        return str;
    }

    // char转byte

    public static byte[] getBytes(char[] chars) {
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = CharBuffer.allocate(chars.length);
        cb.put(chars);
        cb.flip();
        ByteBuffer bb = cs.encode(cb);
        return bb.array();

    }

    // byte转char
    public static char[] getChars(byte[] bytes) {
        Charset cs = Charset.forName("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);
        return cb.array();
    }

    public static char[] byteTochar(byte[] pData, int datalen) {
        char pRet[] = new char[datalen];
        for (int i = 0; i < datalen; i++) {
            pRet[i] = (char) pData[i];
        }
        return pRet;
    }

    public static String OpearTLV(String strDate, String strTag) {
        String strTagDate;
        if (strDate.length() == 0 || strTag.length() == 0) {
            return "";
        }

        String strSlipt[] = strDate.split(strTag);
        int nLen = Integer.parseInt(strSlipt[1].substring(0, 2), 16);
        strTagDate = strSlipt[1].substring(2, 2 + nLen * 2);
        return strTagDate;
    }

    public static int TimeDiffer(int nStratMinute, int nStratSecods, int nEendMinute, int nEndSecods) {
        int nMinuteDiff = 0;
        int nSecodDiff = 0;
        int nCountDiff = 0;
        nMinuteDiff = nEendMinute - nStratMinute;
        nSecodDiff = nEndSecods - nStratSecods;
        nCountDiff = Math.abs(nMinuteDiff) * 60 + Math.abs(nSecodDiff);
        return nCountDiff;
    }

    public static String JoinStr(String str, String join) {
        String strData = new String();
        strData += "02";
        for (int i = 0; i < str.length(); i++) {
            strData += join;
            strData += str.substring(i, i + 1);
        }
        strData += "03";
        return strData;
    }

    public static String JoinStrSmallCard(String str, String join) {
        String strData = new String();
        for (int i = 0; i < str.length(); i++) {
            strData += join;
            strData += str.substring(i, i + 1);
        }
        return strData;
    }

    public static String RemoveStr(String str, String remove) {
        String strData = new String();
        // if (!str.substring(0, 2).equals("02"))
        // if (!str.startsWith("02")) {
        // CGenUtil.OutputLog("数据格式不正确");
        // return "";
        // }
        // if (!str.endsWith("03")) {
        // CGenUtil.OutputLog("数据格式不正确");
        // return "";
        // }
        if (str.length() % 2 != 0) {
            BJCWUtil.OutputLog("数据长度不正确");
            return "";
        }

        for (int i = 0; i < str.length(); i += 2) {
            if (!str.substring(i, i + 1).equals(remove)) {
                BJCWUtil.OutputLog("数据内容不正确");
                return "";
            }
            strData += str.substring(i + 1, i + 2);
        }
        return strData;
    }

    private static void MD5M(byte[] buf, int len, byte[] digest) {
        MessageDigest mdInst;
        try {
            mdInst = MessageDigest.getInstance("MD5");

            // 使用指定的字节更新摘要
            mdInst.update(buf);
            // 获得密文
            byte[] md = mdInst.digest();
            System.arraycopy(md, 0, digest, 0, mdInst.getDigestLength());
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    public static String StrToAscii(String str) {
        byte[] val = StrToHex(str);
        String result = new String(val);
        return result;
    }

    public static boolean judgeData(String result) {
        Log.e("YJL", "result===" + result.length());
        String strReplyLen = result.substring(2, 6);// strReadDate
        int nstrReplyLen = Integer.parseInt(strReplyLen, 16);
        Log.e("YJL", "nstrReplyLen==" + nstrReplyLen);
        int nReplyLen = result.length() - 6;// strReadDate
        Log.e("YJL", "nReplyLen==" + nReplyLen);
        if (nstrReplyLen * 2 != nReplyLen) {
            BJCWUtil.OutputLog("ConnectedThread***************数据不完整");
            return false;
        } else {
            BJCWUtil.OutputLog("ConnectedThread***************数据完整");
            return true;
        }
    }

}
