<!--
    使用方式：
         1，识别语音
            import voice from './voiceUtil'
            components: {'voice': voice},
            <voice ref="v" v-on:getVoiceData="getVoice" ></voice>
            or
            <voice ref="v" v-on:getVoiceData="getVoice"  v-bind:maxTimeIn="time"  v-bind:type="'recognizeVoice'"></voice>
            组件通讯方法getVoiceData                             maxTimeIn(最长录音时间，秒)    type（类型）
            type  识别类型 ：recognizeVoice   固定
            vm.$refs.v.show();   开始录音
            可设置最长录音maxTimeIn(秒)  可选（默认20s）



语音插件（功能，语音转文字，命令识别）：使用base64码传输语音包
依赖说明：
     依赖云之家，XuntongJSBridge
     依赖语音服务器(voiceServer1)http://lapp.leedarson.com:8081/voice/baiduvop

     LZQ  2017-12-11
 -->

<template>
  <div class="voice" v-show="showVoice" @click="esc">
    <div v-show="!loadShow" class="voice_page">
      <div class="voice_body">

        <div class="voice_icon">
          <svg v-show="voice_icon_1"
               style="width: 1.0048828125em; height: 1em;vertical-align: middle;fill: currentColor;overflow: hidden;"
               viewBox="0 0 1029 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="1444">
            <path
              d="M511.104 653.186c52.22 0 94.531-53.779 94.531-120.155v-288.38c0-66.375-42.313-120.157-94.531-120.157-52.223 0-94.534 53.783-94.534 120.157v288.377c0.001 66.376 42.314 120.158 94.534 120.158z"
              fill="" p-id="1445"></path>
            <path
              d="M643.45 533.029c0 92.902-59.254 168.221-132.347 168.221-73.097 0-132.351-75.314-132.351-168.221v-72.098h-37.811v72.098c0 111.313 66.173 202.972 151.254 214.938v97.476H416.57v48.063h189.064v-48.063h-75.627v-97.476C615.088 736 681.262 644.343 681.262 533.029v-72.098h-37.813v72.098h0.001z"
              fill="" p-id="1446"></path>
          </svg>
          <svg v-show="voice_icon_2"
               style="width: 1.0048828125em; height: 1em;vertical-align: middle;fill: currentColor;overflow: hidden;"
               viewBox="0 0 1029 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="1474">
            <path
              d="M511.104 653.186c52.22 0 94.531-53.779 94.531-120.155v-288.38c0-66.375-42.313-120.157-94.531-120.157-52.223 0-94.534 53.783-94.534 120.157v288.377c0.001 66.376 42.314 120.158 94.534 120.158z"
              fill="" p-id="1475"></path>
            <path
              d="M643.45 533.029c0 92.902-59.254 168.221-132.347 168.221-73.097 0-132.351-75.314-132.351-168.221v-72.098h-37.811v72.098c0 111.313 66.173 202.972 151.254 214.938v97.476H416.57v48.063h189.064v-48.063h-75.627v-97.476C615.088 736 681.262 644.343 681.262 533.029v-72.098h-37.813v72.098h0.001zM254.493 317.825c-74.055 109.659-74.055 274.275-0.027 384.875 36.588 5.471 29.478-38.043 29.478-38.043-57.715-89.293-57.694-219.898 0.268-308.515-0.004 0.001 3.173-38.304-29.719-38.317zM766.508 704.675c74.055-109.659 74.055-274.273 0.026-384.874-36.588-5.471-29.479 38.043-29.479 38.043 57.715 89.293 57.694 219.898-0.267 308.516 0.005-0.003-3.173 38.302 29.72 38.315z"
              fill="" p-id="1476"></path>
          </svg>
          <svg v-show="voice_icon_3"
               style="width: 1.0048828125em; height: 1em;vertical-align: middle;fill: currentColor;overflow: hidden;"
               viewBox="0 0 1029 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="1504">
            <path
              d="M511.104 653.186c52.22 0 94.531-53.78 94.531-120.156V244.651c0-66.375-42.313-120.157-94.531-120.157-52.223 0-94.534 53.783-94.534 120.157v288.377c0.001 66.376 42.314 120.158 94.534 120.158zM852.608 230.537c-39.502-0.107-28.779 37.089-28.779 37.089 95.1 137.08 95.021 346.001 0.035 484.083 0 0-12.758 45.301 29.438 37.945 109.882-147.537 109.882-412.095-0.694-559.117z"
              fill="" p-id="1505"></path>
            <path
              d="M643.451 533.029c0 92.903-59.254 168.221-132.347 168.221-73.097 0-132.351-75.314-132.351-168.221v-72.097h-37.81v72.097c0 111.313 66.173 202.972 151.254 214.938v97.475H416.57v48.063h189.064v-48.063h-75.627v-97.475C615.088 736 681.262 644.343 681.262 533.029v-72.097h-37.813v72.097h0.002zM197.154 260.563c4.049-40.502-28.775-37.089-28.775-37.089-110.597 147.022-110.814 411.582-0.708 559.121 35.199 0.105 29.441-37.951 29.441-37.951-94.995-138.079-95.076-347.001 0.042-484.081z"
              fill="" p-id="1506"></path>
            <path
              d="M254.493 317.825c-74.055 109.659-74.055 274.275-0.027 384.875 36.588 5.471 29.478-38.043 29.478-38.043-57.715-89.293-57.695-219.898 0.267-308.515-0.003 0.001 3.174-38.304-29.718-38.317zM766.508 704.675c74.055-109.659 74.055-274.274 0.027-384.874-36.588-5.471-29.479 38.043-29.479 38.043 57.715 89.293 57.695 219.898-0.266 308.515 0.003-0.002-3.175 38.303 29.718 38.316z"
              fill="" p-id="1507"></path>
          </svg>
        </div>
        <div class="voice_time">{{showTimeMinute}}:{{showTimeSecond}}</div>
      </div>
      <br>
      <div class="voice_ok" @click="stopRecord">确定</div>
      <br>
      <div class="esc">点击空白处退出</div>
    </div>
    <div class="voice_loading" v-show="loadShow">
      <div class="loading_icon"></div>
      <div class="loading_text">识别中</div>
    </div>
  </div>
</template>

<script>
  import Vue from 'vue'

  //  let requestAnimationFrame = window.requestAnimationFrame || window.mozRequestAnimationFrame || window.webkitRequestAnimationFrame || window.msRequestAnimationFrame;
  export default {
    name: 'voiceUtil',
    data: function () {
      return {
        voiceServer1: "http://lapp.leedarson.com:8081/voice/baiduvop",   //语音服务器地址
        voiceServer2: "",
        showTimeMinute: "00",           //页面显示的时间 （分）
        showTimeSecond: "00",           //页面显示的时间 （秒）
        countervm: "",                 //页面计数器对象
        showVoice: false,                 //录音页面是否显示   true显示   ，false隐藏
        maxTime: "",                    //最大录音时间
        voice_icon_1: false,
        voice_icon_2: false,
        voice_icon_3: false,
        animationF: '',
        loadShow: false,                    //load  界面是否显示
      };
    },
    methods: {
      //显示时间处理(计数器)  json={maxTime:秒值}
      counter: function (json) {
        let vm = this;

        if (vm.countervm) {
          console.log("录音已存在,不在添加新的录音");
          return
        }
        let maxTime = json.maxTime;
        let oldTime = new Date().getTime();//运行开始时间
        maxTime = maxTime * 1000;    //毫秒
        vm.countervm = setInterval(function () {
          let nowTime = new Date().getTime();
          let i = nowTime - oldTime;
          if (i >= maxTime) {
            vm.stopRecord();
          }
          nowTime = new Date(i);
          let minute = nowTime.getMinutes() ;
          if(minute < 10){
            minute = "0"+ minute ;
          }
          let second = nowTime.getSeconds() ;
          if(second < 10){
            second = "0"+ second;
          }
          vm.showTimeMinute = minute ;
          vm.showTimeSecond = second ;
        }, 1000);
      },
      //关闭计数器
      closeCounter: function () {
        let vm = this;

        if (vm.countervm) {
          clearInterval(vm.countervm);
          vm.restShowTime();
        }
      },
      //语音文本返回调用
      backData: function (result) {
        let vm = this;
        let data = result.data.result;
        data = data.toString();

        console.log("组件输出数据");
        vm.$emit('getVoiceData', data);
        vm.restShowTime();
        vm.showVoice = false;

      },
      //调用语音转文本
      voiceHandle: function (result) {
        let vm = this;
        let voice = result.data;
        let len = 0;
        let size = 0;
        if (voice) {
          voice = result.data.base64;
          len = result.data.len;
          size = result.data.size;
        }

        console.log("调用语音处理");
        let fromData = {
          "voice": voice,
          "len": len,
          "size": size
        };
        vm.loadShow = true;

        vm.$http.post(vm.voiceServer1,fromData,{emulateJSON: true}).then((response)=>{
            vm.loadShow = false;
            vm.backData(response);
            vm.showVoice = false;
        }).catch((error)=>{
          conlose.log("请求异常 \n" +JSON.stringify(error));
          alert("请求异常 网络代码" +error.status )
        });

      },
      //停止录音
      stopRecord: function () {
        let vm = this;
        vm.closeAnimation();
        XuntongJSBridge.call('stopRecord', {
          returnBase64: true //是否返回语音文件base64值， 默认不返回
        }, function (result) {
          if (typeof(result) !== "object") {
            result = JSON.parse(result);
          }
          vm.voiceHandle(result);
        });

      },
      //开始录音
      startRecord: function () {
        let vm = this;
        let maxTime = vm.maxTime;
        vm.counter({
          maxTime: maxTime
        });
        XuntongJSBridge.call('startRecord', {
          returnBase64: true //是否返回语音文件base64值，默认不返回
        }, function (result) {
          if (typeof(result) !== "object") {
            result = JSON.parse(result);
          }
          vm.voiceHandle(result);
        });
        vm.animation();
      },
      //重置显示时间
      restShowTime: function () {
        let vm = this;
        vm.showTimeMinute = "00";
        vm.showTimeSecond = "00";
        if (vm.countervm) {
          clearInterval(vm.countervm);
          vm.countervm = '';
        }
      },
      //显示页面
      show: function () {
        let vm = this;
        if (vm.isCloudHub()) {
          vm.startRecord();
          vm.showVoice = true;
          console.log("执行显示");
        } else {
          alert("请使用云之家打开")
        }
      },
      //开启动画(图标)
      animation(){
        let vm = this;
        vm.voice_icon_1 = false;
        vm.voice_icon_2 = false;
        vm.voice_icon_3 = true;
        vm.animationF = false;

        let interval = setInterval(function () {
          if (vm.voice_icon_1) {
            vm.voice_icon_1 = false;
            vm.voice_icon_2 = true;
          } else if (vm.voice_icon_2) {
            vm.voice_icon_2 = false;
            vm.voice_icon_3 = true;
          } else if (vm.voice_icon_3) {
            vm.voice_icon_3 = false;
            vm.voice_icon_1 = true;
          }
          if (vm.animationF) {
            clearInterval(interval);
          }
        }, 1000 * 0.35);
      },
      //关闭动画
      closeAnimation: function () {
        let vm = this;
        vm.voice_icon_1 = false;
        vm.voice_icon_2 = false;
        vm.voice_icon_3 = true;
        vm.animationF = true;
        vm.closeCounter();
      },
      //点击空白退出语音输入
      esc: function (event) {
        let vm = this;
        let className = event.toElement.className;
        if ((className === "esc") || (className === "voice")) {
          console.log("执行了退出");
          vm.closeAnimation();
          vm.showVoice = false;
        }
      },
      //判断环境是否为云之家
      isCloudHub: function () {
        let userAgent = navigator.userAgent;     //获取用户ua信息,判断OS
        if ((userAgent.indexOf("projectCode") === -1) || (userAgent.indexOf("xtUrl") === -1)) {
          return false;
        } else {
          return true;
        }
      },
    },
    props: [
      'maxTimeIn',     //最大录音时间（秒s）
    ],
    created: function () {
      let vm = this;
      if (vm.maxTimeIn) {
        vm.maxTime = vm.maxTimeIn;
      } else {
        vm.maxTime = 20;
      }
    },
    mounted: function () {
    }


  }
</script>

<style scoped lang="scss">

  $backgroundColor: rgba(76, 177, 232, 0.65);
  .voice {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 9990;
    padding-top: 25vh;
    color: #FFF;
    background-color: rgba(0, 0, 0, 0.5);
    text-align: center;
    font-size: 1.4rem;
    .voice_page {
      width: 50vw;
      display: inline-block;
    }

    .voice_body {
      text-align: center;
      background-color: $backgroundColor;
      border-radius: 5px;
      padding: 15px 0;
    }

    .voice_icon {
      font-size: 6rem;
      line-height: 5rem;
    }

    .voice_time {

    }

    .voice_ok {
      text-align: center;
      padding: 15px;
      background-color: $backgroundColor;
      border-radius: 5px;

    }

    .voice_ok:hover {
      background-color: #7eb4d2;
      border-color: #adadad;
    }

    .esc {
      text-align: center;
    }

    .voice_loading {
      text-align: center;
      position: fixed;
      width: 100%;
      height: 100%;
      top: 0;
      padding-top: 30vh;
    }

    .loading_icon {
      height: 32px;
      width: 32px;
      display: inline-block;
      line-height: 32px;
      animation: mint-spinner-rotate .8s infinite linear;
      webkit-animation: mint-spinner-rotate .8s infinite linear;
      border: 4px solid rgb(204, 204, 204);
      border-bottom-color: transparent;
      border-radius: 50%;
    }

    .loading_text {
      margin-top: 10px;
    }
  }

  //加载动画loading
  @-webkit-keyframes mint-spinner-rotate {
    0% {
      -webkit-transform: rotate(0deg);
      transform: rotate(0deg);
    }
    100% {
      -webkit-transform: rotate(1turn);
      transform: rotate(1turn);
    }
  }

  //加载动画loading
  @keyframes mint-spinner-rotate {
    0% {
      -webkit-transform: rotate(0deg);
      transform: rotate(0deg);
    }
    100% {
      -webkit-transform: rotate(1turn);
      transform: rotate(1turn);
    }
  }
</style>
