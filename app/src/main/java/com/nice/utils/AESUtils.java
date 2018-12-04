package com.nice.utils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;


public class AESUtils {
    static final String algorithmStr = "AES/ECB/PKCS5Padding";

    private static final Object TAG = "AES";

    static private KeyGenerator keyGen;

    static private Cipher cipher;

    static boolean isInited = false;

    private static void init() {
        try {
            /**为指定算法生成一个 KeyGenerator 对象。
             *此类提供（对称）密钥生成器的功能。
             *密钥生成器是使用此类的某个 getInstance 类方法构造的。
             *KeyGenerator 对象可重复使用，也就是说，在生成密钥后，
             *可以重复使用同一 KeyGenerator 对象来生成进一步的密钥。
             *生成密钥的方式有两种：与算法无关的方式，以及特定于算法的方式。
             *两者之间的惟一不同是对象的初始化：
             *与算法无关的初始化
             *所有密钥生成器都具有密钥长度 和随机源 的概念。
             *此 KeyGenerator 类中有一个 init 方法，它可采用这两个通用概念的参数。
             *还有一个只带 keysize 参数的 init 方法，
             *它使用具有最高优先级的提供程序的 SecureRandom 实现作为随机源
             *（如果安装的提供程序都不提供 SecureRandom 实现，则使用系统提供的随机源）。
             *此 KeyGenerator 类还提供一个只带随机源参数的 inti 方法。
             *因为调用上述与算法无关的 init 方法时未指定其他参数，
             *所以由提供程序决定如何处理将与每个密钥相关的特定于算法的参数（如果有）。
             *特定于算法的初始化
             *在已经存在特定于算法的参数集的情况下，
             *有两个具有 AlgorithmParameterSpec 参数的 init 方法。
             *其中一个方法还有一个 SecureRandom 参数，
             *而另一个方法将已安装的高优先级提供程序的 SecureRandom 实现用作随机源
             *（或者作为系统提供的随机源，如果安装的提供程序都不提供 SecureRandom 实现）。
             *如果客户端没有显式地初始化 KeyGenerator（通过调用 init 方法），
             *每个提供程序必须提供（和记录）默认初始化。
             */
            keyGen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // 初始化此密钥生成器，使其具有确定的密钥长度。
        keyGen.init(128); //128位的AES加密
        try {
            // 生成一个实现指定转换的 Cipher 对象。
            cipher = Cipher.getInstance(algorithmStr);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        //标识已经初始化过了的字段
        isInited = true;
    }

    private static byte[] genKey() {
        if (!isInited) {
            init();
        }
        //首先 生成一个密钥(SecretKey),
        //然后,通过这个秘钥,返回基本编码格式的密钥，如果此密钥不支持编码，则返回 null。
        return keyGen.generateKey().getEncoded();
    }

    private static byte[] encrypt(byte[] content, byte[] keyBytes) {
        byte[] encryptedText = null;
        if (!isInited) {
            init();
        }
        /**
         *类 SecretKeySpec
         *可以使用此类来根据一个字节数组构造一个 SecretKey，
         *而无须通过一个（基于 provider 的）SecretKeyFactory。
         *此类仅对能表示为一个字节数组并且没有任何与之相关联的钥参数的原始密钥有用
         *构造方法根据给定的字节数组构造一个密钥。
         *此构造方法不检查给定的字节数组是否指定了一个算法的密钥。
         */
        Key key = new SecretKeySpec(keyBytes, "AES");
        try {
            // 用密钥初始化此 cipher。
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        try {
            //按单部分操作加密或解密数据，或者结束一个多部分操作。(不知道神马意思)
            encryptedText = cipher.doFinal(content);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return encryptedText;
    }

    private static byte[] encrypt(String content, String password) {
        try {
            byte[] keyStr = getKey(password);
            SecretKeySpec key = new SecretKeySpec(keyStr, "AES");
            Cipher cipher = Cipher.getInstance(algorithmStr);//algorithmStr
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);//   ʼ
            byte[] result = cipher.doFinal(byteContent);
            return result; //
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] decrypt(byte[] content, String password) {
        try {
            byte[] keyStr = getKey(password);
            SecretKeySpec key = new SecretKeySpec(keyStr, "AES");
            Cipher cipher = Cipher.getInstance(algorithmStr);//algorithmStr
            cipher.init(Cipher.DECRYPT_MODE, key);//   ʼ
            byte[] result = cipher.doFinal(content);
            return result; //
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] getKey(String password) {
        byte[] rByte = null;
        if (password != null) {
            rByte = password.getBytes();
        } else {
            rByte = new byte[24];
        }
        return rByte;
    }

    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
                    16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    //注意: 这里的password(秘钥必须是16位的)
    private static final String keyBytes = "TIKTOKKEY1368725";

    /**
     * 加密
     */
    public static String encode(String content) {
        //加密之后的字节数组,转成16进制的字符串形式输出
        return parseByte2HexStr(encrypt(content, keyBytes));
    }

    /**
     * 解密
     */
    public static String decode(String content) {
        //解密之前,先将输入的字符串按照16进制转成二进制的字节数组,作为待解密的内容输入
        byte[] b = decrypt(parseHexStr2Byte(content), keyBytes);
        return new String(b);
    }

    //测试用例
    public static void test1() {
        String content = "{\"activationCode\":\"004d68b7564c46d3929592ac51f700dd\",\"installationCode\":\"6db01b75-643e-428c-9597-82dab7d9077e\",\"deviceInfo\":\"OnePlus-OnePlus5-27\"}";
        String pStr = encode(content);
        System.out.println("加密前：" + content);
        System.out.println("加密后:" + pStr);

        String postStr = decode("58C7525204E788E1804C711D5DF6F8456DB606AD06B8784D37897F6578A9D6114CEFA6CE64B38A45F6B949C10A424684530723CA585177ADBBDC479B2778F0959D18864C5D614496740B764879CFE6C53A3E8AB77BF4AFB0E48A99BDD77ED38B9405788A1AD7616654CD7A02C37D324860BADD9D3E2ED9AD6F55F0B92EDE09C6506B25E57D76DD5B77F4E656A56B8F7BC1080AB3FBB6AD74E85CE4816519647C4BA25D2EFBE594E37BC37CDA8BDC72A62A285746C3B2AE482ADAD82ECE29C7FA746C30C1546105F3C5D8DC91BC59A0EFDA4E2A0FFDE7D99506529EB914FF69897D77F490514441C377A864C20BAC1D252DE425610F15A56FC4376A05C4C4C35045B72A6EDC75C5D6DE600479F1ECA58FBE5C92D4E3D5C03607233AD042F4F3E132692BB60D95FA8349D5AE80D606BEE8459F2A66F66B649759A4E16504688212CE2846546C295B1904257D6B92BC13F284A5BF7921E2C4BACE2472269DD22C9951AD18C3C4F73C419F7DADDADEF61E88167B28ECB378E5141D325E6BBDABCF21BC78C52D9D1ECBB6C635425519F74527B99078744189659AF0C4683E13F89F90489C746FE453A874893A76666F9F5246DC27768AAB5113A647ABE747ECBFEE9114ACC6F974C103AB3C04BE0700133E36DF4C2AA035DFF3EAC2A617A6BEE772073AD494D789EFAF7E6B0137DCAD6BD02EE0A47D551445798B17BA2C890C1DEF575244E70A2CA24D7A742DF77D9A4E4B9C669B875429FF8BED1353D50E7997828016A2F327E67B4A8FF23927D3E434ED000EC715AF0C5D6517B2E7F7131F544B98DF3B0DECECFB1B01EA2DF2760969B573DDCA16A19E3A4D6A731520B06258B741D49A37E307D3D461416371BC0CC2130BD4DE4E6E1E73092F74F7B4D8E3338ED9F3DDF7A2BACC69CA3BCE0D8B2952B186CB8977972F8CAF3A9365AFD4880108E28D28019DFBBFE7D1BB3300AA9372BCFE4B9FB98ECEADFA20E049CD4D50F1ADADABA71EE6556486CA3B615668098FE4CF394D56017E56AD41A97C3D3F6454062A43395E8D5194DDA0556749515AD54F00A33C3B8C61EABB294B59A6C2968C33178BF460A6A37FCE1463C2A6110A384E86B02490B850678A3A03B996815F08C6F38CEA51BF942DB59908B0B16FBB7D2B9C60BADD9D3E2ED9AD6F55F0B92EDE09C6506B25E57D76DD5B77F4E656A56B8F7B42B44E5011F137EE3B9BD84566FB4E5D214C5F4D19B9A5A9D527BE7595F6C5725062FE5E7D78695695335D9251B6BA2AA7F0D8CDD67D546F5EBCBCDB01EA52E866D8E1276111D728B4CA5A6DE660F1C8DF4C2AA035DFF3EAC2A617A6BEE772073AD494D789EFAF7E6B0137DCAD6BD02EE0A47D551445798B17BA2C890C1DEF57BA32A2DD1385240B75F94956CE39D74AD4AD8D4FFD8670267927379BB1AB49578C05FBED76F0BE4F27900BDD71F3AA8E1A3B1BB02B5FC08D7AA279B874763C84DF3B0DECECFB1B01EA2DF2760969B573DDCA16A19E3A4D6A731520B06258B741D49A37E307D3D461416371BC0CC2130BA7FD299CA681F18503F27809F91F88B5171B46A684F34F90534616509CD7B9C3AB05BBDC33889122F4905A6322F512E4CF404914D91B716CBD33956B4A61DD658904C5B605C918E967FCDD5AA3A5BAC7EDF4AFAD686FEDA12E173C8FEE2E7070AAE95A43C600A18C0EEB7CCE978B73A0FD51794268E8DA209C4010F2D4763D8CB814882DF2BAD9A2846752D2D9DA596CC82A9FCB8A2FA3D133451E49406F470872224A44247D0FAB683A60689BD693567675390AEE12A03436FA98517F30FEC62F57E87B8B6BA0DE845CC7C1471488327E97A3DC7EDF3EC726683339499DE0B1634A3B514C5E8DCE3AD5594751784BAD8744F163A506F3182A3DB2F763BBD15A0D1D4EFAA289A8BAC72A84051C2D1149B8824679AF3FE54347CB405CF38CAA22D2F7A240F452CA75A59D8AB281027A6327A6243C4551372F21FF3DAAABB649327B640056BC6C8800C6F07B1399D0B540B2C0767DF3591592CC40B049B4C87DFBEE360B809EF984EADD31B508C31D0339B93E065517FECC64BC6B4A4BA2B64FF4");
        System.out.println("解密后：" + postStr);
    }

    public static void main(String[] args) {
        test1();
        System.out.println(UUID.randomUUID().toString());
    }
}
