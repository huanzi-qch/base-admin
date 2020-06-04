/**
 * 加解密操作简单封装一下
 */
aesUtil = {

    //获取key，
    genKey : function (length = 16) {
        let random = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        let str = "";
        for (let i = 0; i < length; i++) {
            str  = str + random.charAt(Math.random() * random.length)
        }
        return str;
    },

    //加密
    encrypt : function (plaintext,key) {
        if (plaintext instanceof Object) {
            //JSON.stringify
            plaintext = JSON.stringify(plaintext)
        }
        let encrypted = CryptoJS.AES.encrypt(CryptoJS.enc.Utf8.parse(plaintext), CryptoJS.enc.Utf8.parse(key), {mode:CryptoJS.mode.ECB,padding: CryptoJS.pad.Pkcs7});
        return encrypted.toString();
    },

    //解密
    decrypt : function (ciphertext,key) {
        let decrypt = CryptoJS.AES.decrypt(ciphertext, CryptoJS.enc.Utf8.parse(key), {mode:CryptoJS.mode.ECB,padding: CryptoJS.pad.Pkcs7});
        let decString = CryptoJS.enc.Utf8.stringify(decrypt).toString();
        if(decString.charAt(0) === "{" || decString.charAt(0) === "[" ){
            //JSON.parse
            decString = JSON.parse(decString);
        }
        return decString;
    }
};
rsaUtil = {
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

/**
 * 常用工具方法
 */
commonUtil = {
    /**
     * 扩展jquery对象方法
     */
    jqueryFnExtend : function(){
        /**
         * 拓展表单对象：用于将对象序列化为JSON对象
         */
        $.fn.serializeObject = function () {
            var o = {};
            var a = this.serializeArray();
            $.each(a, function () {
                if (o[this.name]) {
                    if (!o[this.name].push) {
                        o[this.name] = [o[this.name]];
                    }
                    o[this.name].push(this.value || '');
                } else {
                    o[this.name] = this.value || '';
                }
            });
            return o;
        };

        /**
         * 拓展表单对象：表单自动回显
         * 使用参考：$("#form1").form({"id":"112","username":"ff","password":"111","type":"admin"});
         */
        $.fn.form = function (data) {
            let form = $(this);
            for (let i in data) {
                let name = i;
                let value = data[i];
                if (name !== "" && value !== "") {
                    valuAtion(name, value);
                }
            }

            function valuAtion(name, value) {
                if (form.length < 1) {
                    return;
                }
                if (form.find("[name='" + name + "']").length < 1) {
                    return;
                }
                let input = form.find("[name='" + name + "']")[0];
                if ($.inArray(input.type, ["text", "password", "hidden", "select-one", "textarea"]) > -1) {
                    $(input).val(value);
                } else if (input.type === "radio" || input.type === "checkbox") {
                    form.find("[name='" + name + "'][value='" + value + "']").attr("checked", true);
                }
            }
        };

        /**
         * 拓展jQuery对象：快速AJAX Delete删除
         */
        $.delete = function (url, params, callback) {
            if (!params || typeof params === 'string') {
                throw new Error('Error Params：' + params);
            }

            $.ajax({
                url: url,
                type: "DELETE",
                contentType: 'application/json',//发送格式（JSON串）
                data: JSON.stringify(params),//发送参数（JSON串）
                success: function (result) {
                    callback && callback(result);
                }
            });
        };
    },

    /**
     * 获取当前时间，并格式化输出为：2018-05-18 14:21:46
     */
    getNowTime: function () {
        var time = new Date();
        var year = time.getFullYear();//获取年
        var month = time.getMonth() + 1;//或者月
        var day = time.getDate();//或者天

        var hour = time.getHours();//获取小时
        var minu = time.getMinutes();//获取分钟
        var second = time.getSeconds();//或者秒
        var data = year + "-";
        if (month < 10) {
            data += "0";
        }
        data += month + "-";
        if (day < 10) {
            data += "0"
        }
        data += day + " ";
        if (hour < 10) {
            data += "0"
        }


        data += hour + ":";
        if (minu < 10) {
            data += "0"
        }
        data += minu + ":";
        if (second < 10) {
            data += "0"
        }
        data += second;
        return data;
    },

    /**
     * 将我们响应的系统菜单数据转换成符合layui的tree结构
     * @param arrar  旧数据
     * @returns {Array} 新数据
     */
    updateKeyForLayuiTree: function (arrar) {
        let newArray = [];
        for (let i = 0; i < arrar.length; i++) {
            let obj1 = {};
            let obj = arrar[i];
            obj1.id = obj.menuId;
            obj1.title = obj.menuName;
            obj1.href = obj.menuPath;

            if (obj.children.length > 0) {
                obj1.children = this.updateKeyForLayuiTree(obj.children);
            }
            newArray.push(obj1);
        }
        return newArray
    },

    /**
     * 在所有系统菜单上勾选用户菜单
     * @param arrTree
     * @param userTreeString
     */
    checkedForLayuiTree:function (arrTree, userTreeString) {
        for(let tree of arrTree){
            //默认全部展开
            tree.spread=true;
            //递归子节点
            if(tree.children && tree.children.length > 0){
                tree.children = this.checkedForLayuiTree(tree.children,userTreeString);
            }else{
                //是否包含（勾选子节点默认会勾上父节点，如果勾选父节点，默认会全部勾上所有子节点）
                if(userTreeString.search(tree.id) !== -1){
                    tree.checked = true;
                }
            }
        }
        return arrTree;
    }
};



/* 以下代码统一执行  */

//扩展jquery对象方法
commonUtil.jqueryFnExtend();

//判断api加密开关
if(sessionStorage.getItem('sysApiEncrypt') === "Y"){
    //获取前端RSA公钥密码、AES的key，并放到window
    let genKeyPair = rsaUtil.genKeyPair();
    window.jsPublicKey = genKeyPair.publicKey;
    window.jsPrivateKey = genKeyPair.privateKey;

    //重写ajax加密，并保留原始ajax，命名为_ajax
    let _ajax = $.ajax;//首先备份下jquery的ajax方法
    $.ajax = function (opt) {
        //默认值
        // opt = {
        //     type: 'post',
        //     url: url,
        //     contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
        //     dataType: 'json',
        //     data: data,
        //     success: success,
        //     error: function (xhr, status, error) {
        //         console.log("ajax错误！");
        //     }
        // };

        //备份opt中error和success方法
        let fn = {
            error: function (XMLHttpRequest, textStatus, errorThrown) {
            },
            success: function (data, textStatus) {
            }
        };
        if (opt.error) {
            fn.error = opt.error;
        }
        if (opt.success) {
            fn.success = opt.success;
        }

        //加密再传输
        if (opt.type.toLowerCase() === "post") {
            let data = opt.data;
            //发送请求之前随机获取AES的key
            let aesKey = aesUtil.genKey();
            data = {
                data: aesUtil.encrypt(data, aesKey),//AES加密后的数据
                aesKey: rsaUtil.encrypt(aesKey, sessionStorage.getItem('javaPublicKey')),//后端RSA公钥加密后的AES的key
                publicKey: window.jsPublicKey//前端公钥
            };
            opt.data = data;
        }

        //扩展增强处理
        let _opt = $.extend(opt, {
            //成功回调方法增强处理
            success: function (data, textStatus) {
                if (opt.type.toLowerCase() === "post") {
                    data = aesUtil.decrypt(data.data.data, rsaUtil.decrypt(data.data.aesKey, window.jsPrivateKey));
                }
                //先获取明文aesKey，再用明文key去解密数据
                fn.success(data, textStatus);
            }
        });
        return _ajax(_opt);
    };
}



