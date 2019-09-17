/**
 * 简单封装一下
 */
var rsaUtil = {
    //RSA 位数，这里要跟后端对应
    bits: 1024,

    //当前JSEncrypted对象
    thisKeyPair: {},

    //生成密钥对(公钥和私钥)
    genKeyPair: function (bits = rsaUtil.bits) {
        let genKeyPair = {};
        rsaUtil.thisKeyPair = new JSEncrypt({default_key_size: bits});

        //获取私钥
        genKeyPair.privateKey = rsaUtil.thisKeyPair.getPrivateKey();

        //获取公钥
        genKeyPair.publicKey = rsaUtil.thisKeyPair.getPublicKey();

        return genKeyPair;
    },

    //公钥加密
    encrypt: function (plaintext, publicKey) {
        if (plaintext instanceof Object) {
            //1、JSON.stringify
            plaintext = JSON.stringify(plaintext)
        }
        publicKey && rsaUtil.thisKeyPair.setPublicKey(publicKey);
        return rsaUtil.thisKeyPair.encrypt(JSON.stringify(plaintext));
    },

    //私钥解密
    decrypt: function (ciphertext, privateKey) {
        privateKey && rsaUtil.thisKeyPair.setPrivateKey(privateKey);
        let decString = rsaUtil.thisKeyPair.decrypt(ciphertext);
        if(decString.charAt(0) === "{" || decString.charAt(0) === "[" ){
            //JSON.parse
            decString = JSON.parse(decString);
        }
        return decString;
    }
};

/*

//普通字符串
var crypt = new JSEncrypt({default_key_size:1024,log:true});

var text = "huanzi.qch@qq.com:欢子";

crypt.setPublicKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCDgFljnucpFCPadEWgkdabKGs58SLa15UWE5Fo+emln/Dvmzw1IhrMEOAhOe3qwkDJMuqDaNj7awaXFrhDYdLR6n8/Vn4ABtpAV7U7FSAhKCQrrMe+oX3jCe5rvZm+SpMO4VH9JJLDgaYVAJ4Z0KIDJCq4vVdpSaqnrnAmkbs/4wIDAQAB");

var enc = crypt.encrypt(text);

crypt.setPrivateKey("MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIOAWWOe5ykUI9p0RaCR1psoaznxItrXlRYTkWj56aWf8O+bPDUiGswQ4CE57erCQMky6oNo2PtrBpcWuENh0tHqfz9WfgAG2kBXtTsVICEoJCusx76hfeMJ7mu9mb5Kkw7hUf0kksOBphUAnhnQogMkKri9V2lJqqeucCaRuz/jAgMBAAECgYBrSWMkuPdF942M5IIOEA40RpS9EDRssaiL+2kOaZ8rpsMu6csEWo/cYARMd3PZraSnwme80OT+swIMcNfi+cPCnS59oao075UJmXpvZRxO4k2dTKEdrg+gQRVc5IGQGG5/1qFDSdFdK4G91HFoa3j/62Hw4EfDAK4//kePj5tQ4QJBAMoyHgiDHxOAGpC+KMT3VtxFmHIX8qhjDD1K9Crrr8R3tKYCuoLuw2bnQz/scGI2AzKyOhxy91vaD8GGgg9LVdMCQQCmfmvuf0whu10H7k94WsGFq6ABu8xhEicGhm8QzaTWZ8PJxBVgZNR4CVhPCiCHaYvp0gaRakIsUZCGla8keNOxAkBld3EK5IIbzxWFvWfIMDcuot41oz7qsYna2Fpfj5bNCSVmicf/HMKCSVu+IHDlWCWSs03mKOto9K0jeNSbyDpxAkEAnP9jeY9SVBCg5kSjbaNvD0RKargTPonmgPGts9OnF4Lbjdw4KCCMdfCh9E5hZC9z/vXMubzQT4hOV4q4kBTb0QJARUurKdkeBVKJ3Ly518Mneilp6/47684e7NPIa2JiZOcm32CHXbY23f1uKC64EKzUQCOxiokNFqL7VT3ufsbaOQ==");

var dec = crypt.decrypt(enc);

console.log(dec);


rsaUtil.genKeyPair();



rsaUtil.genKeyPair();

//复杂对象
// var text = {userName: "huanzi欢子", password: 123456};

//普通字符串
var text = "huanzi.qch@qq.com:欢子";

let privateKey1 = rsaUtil.thisKeyPair.getPrivateKey();
let publicKey1 = rsaUtil.thisKeyPair.getPublicKey();
console.log(privateKey1);
console.log(publicKey1);

var enc = rsaUtil.encrypt(text,publicKey1);

var dec = rsaUtil.decrypt(enc,privateKey1);

console.log(dec);

*/

// let e = "IdOUwLMcu6M16PykUSmD+dXkGQWC7L7dblq5SIOwUQpzzFXzuYHHljyZAm4I9jAc2PR8M6rph9BGyjklgdP2E5i3qzNTvyuVfDsACFB7X4YJ5GjEvPuipefHqa+DyiKP1pYyKOfuOsUMzfQBdz6BuHCTRp+wTlAwtOVbudQSBbQ=";
// var crypt = new JSEncrypt({default_key_size:1024,log:true});
// crypt.setPrivateKey("MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJFIKzaCYP0Rwo6a38sXYASS2xjVC5A59CKoXXGxtqz/wEK861tZTuz5c7w08fqz3akqPKX6Qq0ucDhym7L2g4IoiHVPQ+cROmnM+lR/+n6hh35vq5c9HYD0wvxI3IUg+w4kJeJvU1lj5ojlPaK0H7jAHp1JTGmoLCCwsA2Q6Nm3AgMBAAECgYBN9cw6k/w30S/2FmXBNenwil3IFYr++hpn/rEmMZc1fohNR9OEUYho4fOZK1AumBZ6kI+7AXCGPkU6BUZH4sDuaCfnyCc4HiD/NaHL9H4VKGmFMeB7WGVQ8imwS7dd8xzBxxDq2tnPgONgwGPpJhGfn0rjo8QsAuGsXIxdRdfmAQJBAM3Iup2hECr+gadjpvwm1u84IGT6UjOmS0hsjOogiBbaXn7kzqYMcJvv8QSiGDiGU+9b8IaB+kq5MsxaEgyYNXkCQQC0u+LgHV0IhrMa3bZv9DI/ew+TJ0YFRJgJFiA/dvfWr8qjfM/qUgPXMyU+crUJuuVGUXH0HfH+E8d4ThuHp6yvAkA6FuO3A3RQ7q8i1VqZ60zZEnryhkcWFWdC9oGAxdYkvVfXcjdzYCeZnU3oBOnZavHeq5FmJuLEnSrdzL8zlvPhAkArmvlOIUeE+xrTbJLLyBsnfcVwN5WJR9d2ucPmDAoX3ioB7+cqHceXcFuYCYzsYA9ZO+VGhMMSDiueNxolTKs5AkEAslRX9AJGCebpF0AOdacbxclJqFdpdt78HaTdEbB6Sae9bV5uMyGxIEnnRwrVOM4LVFLahuM4RlBaoEWpEDKs6w==");
// var dec = crypt.decrypt(e);
// console.log(dec);

// rsaUtil.thisKeyPair.setKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCsvbMEKP39w9rcqs25zoIMdFLrNIgcmJxMXua6e9p98EBxCucKuFDfe4aleV5BOg8fZTF6gCRHmrrvyVshr8KRxLb6+A/XX3K71eF6PoJROi8p7zxTUq0NYoQTC2pfwAAYnN6CTPsCRvb0tmD15jreY8lj/ptIRmoEk/tOVmS8LwIDAQAB");
// rsaUtil.decrypt("lGnQ2T/Dp5vBKOWLvUPArPqpFfxDHv2Ld7iuLVRCUh/Y01/2rnqtzKHyO7yrbHyVsbu+hhDNBrdHhEWCRwgHPtrKTF9u2YU5T6B3OZ9yEnNlPNnONZk5xiKA4XMBtino3BFh/y5xPXYSUc/5kYDNhmNOBp1F7BLn+J3urJzjoOM=", "MIICXAIBAAKBgQCsvbMEKP39w9rcqs25zoIMdFLrNIgcmJxMXua6e9p98EBxCucKuFDfe4aleV5BOg8fZTF6gCRHmrrvyVshr8KRxLb6+A/XX3K71eF6PoJROi8p7zxTUq0NYoQTC2pfwAAYnN6CTPsCRvb0tmD15jreY8lj/ptIRmoEk/tOVmS8LwIDAQABAoGBAIj/9p+hBzghAk6q5Hit0LSh+TVzt1O+sY9cYO+b1QC279T3yZ/V6MYmCPbqGBfxPRcwQZqfUKa9j+8nKjDAXbXCivveLrhPBIHrDvkDyPCbXCBAYJUxFeuAx2DU7s2K1liP0T2DCjaOnCA/sD51SYnc+MKHpjBZZY4/+0SsA9CJAkEA21ibkaKFrtGDYl2tnL6JsEYl5Z7pLq0uQKLql7e9FqLX+2XEk5azyNivIKFQ9o71IecaCvDaMBbZYaxztcAMpQJBAMmbX/X0t4VaOmslugK9A+Bk40q/TrLgLLir9lYAE+jlS8BMv0Jttf4qplFg81qAFeMz41ZH1NM9CpLrVgS/KUMCQF7Z7kwH768tQpdi0xSZAImNjA3DripVEU86JB//gHEtciBwXZVE8fHEYdbGa3BzWWWTvhtFE9T/zHkETfUmW1ECQEj9XrWeXp8B9qp6IykAo5mnDP8v2d86+BX39BxYtNyZv14kqw3yyHP3nvVRg2ldfA8g5wqCwROlQuHEIAVh7B8CQEFWc6sORKkQcneoR9O4uIjb9XsK5kHIizajnc76JWAlvFlTu3QmArnlcgJMc9BpeQiNv4uhWypkc6sWe0+H1AY=");

