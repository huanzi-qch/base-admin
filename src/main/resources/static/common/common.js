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
        return rsaUtil.thisKeyPair.encrypt(plaintext);
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
 * jQuery扩展
 */
jQueryExtend = {
    /**
     * 是否已经进行jq的ajax加密重写
     */
    ajaxExtendFlag : false,
    /**
     * jq的ajax备份
     */
    jqAjax : $.ajax,

    /**
     * 扩展jquery对象方法
     */
    fnExtend : function(){
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
                valuAtion(i, data[i]);
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
     * 重写jq的ajax加密，并保留原始ajax，命名为_ajax
     */
    ajaxExtend : function(){
        //判断api加密开关
        if(sessionStorage.getItem('sysApiEncrypt') === "Y" && !jQueryExtend.ajaxExtendFlag){
            jQueryExtend.ajaxExtendFlag = true;
            let _ajax = $.ajax;
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
                            //先获取明文aesKey，再用明文key去解密数据
                            data = aesUtil.decrypt(data.data.data, rsaUtil.decrypt(data.data.aesKey, window.jsPrivateKey));
                        }

                        //统一异常提示（仅控制台，页面处理业务自己决定）
                        if(!data.flag){
                            console.error(data.msg);
                        }

                        fn.success(data, textStatus);
                    }
                });
                return _ajax(_opt);
            };
        }
    },
};

/**
 * 常用工具方法
 */
commonUtil = {
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
     */
    updateKeyForLayuiTree: function (arrar) {
        let newArray = [];
        for (let i = 0; i < arrar.length; i++) {
            let obj1 = {};
            let obj = arrar[i];
            obj1.id = obj.menuId;
            obj1.title = obj.menuName;
            obj1.href = obj.menuPath;
            //自定义数据
            obj1.sortWeight = obj.sortWeight;

            if (obj.children && obj.children.length > 0) {
                obj1.children = this.updateKeyForLayuiTree(obj.children);
            }
            newArray.push(obj1);
        }
        return newArray
    },

    /**
     * 在所有系统菜单上勾选用户菜单
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
    },

    /**
     * 递归获取菜单项
     */
    getChildrenByTree:function(children) {
        let menuIdList = [];
        for (let check of children) {
            menuIdList.push(check.id);
            if (check.children && check.children.length > 0) {
                menuIdList = menuIdList.concat(commonUtil.getChildrenByTree(check.children));
            }
        }
        return menuIdList;
    },

    /**
     * 修改密码
     */
    updatePassword:function(closeBtn) {
        let msg = "新密码";
        if("Y" === sessionStorage.getItem('sysCheckPwdEncrypt')){
            msg = "数字、大小写字母、特殊字符且长度在6-12之间";
        }
        let html = "<form id=\"updatePassword\" class=\"layui-form layui-form-pane\">\n" +
            "\t<div class=\"layui-form-item\">\n" +
            "\t\t<label class=\"layui-form-label\" style='width: 110px !important;'>原密码</label>\n" +
            "\t\t<div class=\"layui-input-block\">\n" +
            "\t\t\t<input type=\"text\" id=\"oldPassword\" name=\"oldPassword\" autocomplete=\"off\"\n" +
            "\t\t\t\t   placeholder=\"原密码\" class=\"layui-input\">\n" +
            "\t\t</div>\n" +
            "\t</div>\n" +
            "\t<div class=\"layui-form-item\">\n" +
            "\t\t<label class=\"layui-form-label\"  style='width: 110px !important;'>新密码</label>\n" +
            "\t\t<div class=\"layui-input-block\">\n" +
            "\t\t\t<input type=\"text\" id=\"newPassword\" name=\"newPassword\" autocomplete=\"off\"\n" +
            "\t\t\t\t   placeholder=\""+msg+"\" class=\"layui-input\">\n" +
            "\t\t</div>\n" +
            "\t</div>\n" +
            "\t<div class=\"layui-form-item\">\n" +
            "\t\t<div class=\"layui-input-block\">\n" +
            "\t\t\t<a class=\"layui-btn\" onclick=\"" +
            "    if(!$('#oldPassword').val()){\n" +
            "            layer.msg('原密码不能为空！', {icon: 2,time: 2000});\n" +
            "            return;\n" +
            "    }\n" +
            "    if(!$('#newPassword').val()){\n" +
            "            layer.msg('新密码不能为空！', {icon: 2,time: 2000});\n" +
            "            return;\n" +
            "    }\n" +
            "    $.post(ctx + '/user/updatePassword', $('#updatePassword').serializeObject(), function (data) {\n" +
            "        if (data.flag) {\n" +
            "            layer.msg('修改密码成功，请重新登录系统！', {icon: 1, time: 2000}, function () {\n" +
            "                window.parent.location.href = ctx + '/logout';\n" +
            "            });\n" +
            "        }else{\n" +
            "            layer.msg(data.msg, {icon: 2, time: 2000}, function () {});\n" +
            "        }\n" +
            "    });" +
            "\">修改</a>\n" +
            "\t\t</div>\n" +
            "\t</div>\n" +
            "</form>";
        //iframe层-父子操作
        layer.open({
            title: '修改密码',
            type: 1,
            area: ['430px', '250px'],
            fixed: false, //固定
            closeBtn:closeBtn,//关闭按钮
            maxmin: false,//最大最小化
            content: html
        });
    },
};


/* 以下代码所有页面统一执行  */

//扩展jquery对象方法
jQueryExtend.fnExtend();

//获取前端RSA公钥密码、AES的key，并放到window
let genKeyPair = rsaUtil.genKeyPair();
window.jsPublicKey = genKeyPair.publicKey;
window.jsPrivateKey = genKeyPair.privateKey;

//重写jq的ajax加密
jQueryExtend.ajaxExtend();



