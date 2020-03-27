package com.telpo.tps900_demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.printer.UsbThermalPrinter;
import com.telpo.tps550.api.util.StringUtil;
import com.telpo.tps550.api.util.SystemUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

public class ActivityPrinter extends Activity {
    TextView title_tv;
    private final int NOPAPER = 3;
    private final int LOWBATTERY = 4;
    private final int OVERHEAT = 12;
    Button btn1,print_paperWalk,print_barcode,print_qrcode,button_print_picture,print_content;
    EditText set_paperWalk,set_textsize,set_printGray,set_content,set_Barcode,set_Qrcode;
    UsbThermalPrinter usbThermalPrinter = new UsbThermalPrinter(ActivityPrinter.this);
    private String picturePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.bmp";
    MyHandler handler;
    private String Result;
    private boolean LowBattery = false;
    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NOPAPER:
                    AlertDialog.Builder dlg = new AlertDialog.Builder(ActivityPrinter.this);
                    dlg.setTitle(getString(R.string.noPaper));
                    dlg.setMessage(getString(R.string.noPaperNotice));
                    dlg.setCancelable(false);
                    dlg.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dlg.show();
                    break;
                case LOWBATTERY:
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ActivityPrinter.this);
                    alertDialog.setTitle(R.string.operation_result);
                    alertDialog.setMessage(getString(R.string.LowBattery));
                    alertDialog.setPositiveButton(getString(R.string.dialog_comfirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    alertDialog.show();
                    break;

                case OVERHEAT:
                    AlertDialog.Builder overHeatDialog = new AlertDialog.Builder(ActivityPrinter.this);
                    overHeatDialog.setTitle(R.string.operation_result);
                    overHeatDialog.setMessage(getString(R.string.overTemp));
                    overHeatDialog.setPositiveButton(getString(R.string.dialog_comfirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    overHeatDialog.show();
                    break;
                default:
                    Toast.makeText(ActivityPrinter.this, "Print Error!", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        initview();
        savepic();
        handler = new MyHandler();
        IntentFilter pIntentFilter = new IntentFilter();
        pIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        pIntentFilter.addAction("android.intent.action.BATTERY_CAPACITY_EVENT");
        registerReceiver(printReceive, pIntentFilter);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    usbThermalPrinter.start(1);
                } catch (TelpoException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void initview() {
        title_tv=findViewById(R.id.title_tv);
        title_tv.setText("Print Test");
        btn1=findViewById(R.id.btn1);
        print_paperWalk=findViewById(R.id.print_paperWalk);
        print_barcode=findViewById(R.id.print_barcode);
        print_qrcode=findViewById(R.id.print_qrcode);
        button_print_picture=findViewById(R.id.button_print_picture);
        print_content=findViewById(R.id.print_content);
        set_paperWalk=findViewById(R.id.set_paperWalk);
        set_textsize=findViewById(R.id.set_textsize);
        set_printGray=findViewById(R.id.set_printGray);
        set_content=findViewById(R.id.set_content);
        set_Barcode=findViewById(R.id.set_Barcode);
        set_Qrcode=findViewById(R.id.set_Qrcode);


        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (LowBattery == true) {
                            handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));
                        } else {
                        try {
                            usbThermalPrinter.reset();
                            usbThermalPrinter.setMonoSpace(true);
                            usbThermalPrinter.setGray(7);
                            usbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
                            Bitmap bitmap1= BitmapFactory.decodeResource(ActivityPrinter.this.getResources(),R.mipmap.telpoe);
                            Bitmap bitmap2 = ThumbnailUtils.extractThumbnail(bitmap1, 244, 116);
                            usbThermalPrinter.printLogo(bitmap2,true);
                            usbThermalPrinter.setTextSize(30);
                            usbThermalPrinter.addString("POS SALES SLIP\n");
                            usbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_LEFT);
                            usbThermalPrinter.setTextSize(24);
                            usbThermalPrinter.addString("MERCHANT NAME:             Telpo");
                            usbThermalPrinter.addString("MERCHANT NO:                  01");
                            usbThermalPrinter.addString("TERMINAL NO:                  02");
                            int i = usbThermalPrinter.measureText("CARD NO:" + "653256689565545");
                            int i1 = usbThermalPrinter.measureText(" ");
                            int SpaceNumber=(384-i)/i1;
                            String spaceString = "";
                            for (int j=0;j<SpaceNumber;j++){
                                spaceString+=" ";
                            }

                            usbThermalPrinter.addString("CARD NO:"+spaceString+"653256689565545");
                            usbThermalPrinter.addString("TRANS TYPE:                GOODS");
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                            String str = formatter.format(curDate);
                            usbThermalPrinter.addString("DATE/TIME:   "+str);
                            usbThermalPrinter.addString("EXP DATE:             2019-12-30" );
                            usbThermalPrinter.addString("BATCH NO:             2019000168");
                            usbThermalPrinter.addString("REFER NO:             2019001232");
                            i = usbThermalPrinter.measureText("AMOUNT:" + "$"+ "32.30");
                            i1 = usbThermalPrinter.measureText(" ");
                            SpaceNumber=(384-i)/i1;
                            spaceString = "";
                            for (int j=0;j<SpaceNumber;j++){
                                spaceString+=" ";
                            }
                            usbThermalPrinter.addString("AMOUNT:" + spaceString +"$"+ "32.30");
                            usbThermalPrinter.printString();
                            usbThermalPrinter.walkPaper(10);
                        } catch (TelpoException e) {
                                e.printStackTrace();
                                Result = e.toString();
                                if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
                                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
                                } else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
                                    handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
                                }
                            }
                        }
                    }
                }).start();
            }
        });

        print_paperWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (LowBattery == true) {
                            handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));
                        } else {
                            String exditText;
                            exditText = set_paperWalk.getText().toString();
                            if (exditText == null || exditText.length() == 0) {
                                Toast.makeText(ActivityPrinter.this, getString(R.string.empty), Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (Integer.parseInt(exditText) < 1 || Integer.parseInt(exditText) > 255) {
                                Toast.makeText(ActivityPrinter.this, getString(R.string.walk_paper_intput_value), Toast.LENGTH_LONG).show();
                                return;
                            }
                            int paperWalk = Integer.parseInt(exditText);
                            try {
                                usbThermalPrinter.walkPaper(paperWalk);
                            } catch (TelpoException e) {
                                e.printStackTrace();
                                Result = e.toString();
                                if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
                                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
                                } else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
                                    handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
                                }
                            }


                        }
                    }
                }).start();

            }
        });


        print_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (LowBattery == true) {
                            handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));
                        } else {
                            int textsize= Integer.parseInt(set_textsize.getText().toString());
                            int gray=Integer.parseInt(set_printGray.getText().toString());
                            String content=set_content.getText().toString();
                            try {
                                usbThermalPrinter.reset();
                                usbThermalPrinter.setMonoSpace(true);
                                usbThermalPrinter.setTextSize(textsize);
                               // usbThermalPrinter.setHighlight(true);
                                usbThermalPrinter.setGray(gray);
                                usbThermalPrinter.addString(content);
                                usbThermalPrinter.printString();
                                usbThermalPrinter.walkPaper(10);
                            } catch (TelpoException e) {
                                e.printStackTrace();
                                Result = e.toString();
                                if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
                                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
                                } else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
                                    handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
                                }
                            }
                        }
                    }
                }).start();



            }
        });

        print_barcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (LowBattery == true) {
                            handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));
                        } else {
                            String barcodeStr=set_Barcode.getText().toString();

                            try {
                                Bitmap bitmap = CreateCode(barcodeStr, BarcodeFormat.CODE_128, 320, 120);
                                usbThermalPrinter.reset();
                                usbThermalPrinter.setMonoSpace(true);
                                usbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
                                usbThermalPrinter.printLogo(bitmap,false);
                                usbThermalPrinter.walkPaper(10);
                            } catch (TelpoException e) {
                                e.printStackTrace();
                                Result = e.toString();
                                if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
                                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
                                } else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
                                    handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
                                }
                            } catch (WriterException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        });


        print_qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (LowBattery == true) {
                            handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));
                        } else {
                            String qrcodeStr=set_Qrcode.getText().toString();

                            try {
                                Bitmap bitmap = CreateCode(qrcodeStr, BarcodeFormat.QR_CODE, 256, 256);
                                usbThermalPrinter.reset();
                                usbThermalPrinter.setGray(7);
                                usbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
                                usbThermalPrinter.printLogo(bitmap,false);
                                usbThermalPrinter.walkPaper(10);
                            } catch (TelpoException e) {
                                e.printStackTrace();
                                Result = e.toString();
                                if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
                                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
                                } else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
                                    handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
                                }
                            } catch (WriterException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        });

        button_print_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (LowBattery == true) {
                            handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));
                        } else {


                            try {
                                usbThermalPrinter.reset();
                                usbThermalPrinter.setGray(7);
                                usbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
                                File file = new File(picturePath);
                                if (file.exists()) {
                                    usbThermalPrinter.printLogo(BitmapFactory.decodeFile(picturePath),false);
                                    usbThermalPrinter.walkPaper(20);
                                } else {
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            Toast.makeText(ActivityPrinter.this, getString(R.string.not_find_picture), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            } catch (TelpoException e) {
                                e.printStackTrace();
                                Result = e.toString();
                                if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
                                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
                                } else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
                                    handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
                                }
                            }
                        }
                    }
                }).start();
            }
        });

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(printReceive);
        usbThermalPrinter.stop();
        super.onDestroy();
    }

    private void savepic() {
        File file = new File(picturePath);
        if (!file.exists()) {
            InputStream inputStream = null;
            FileOutputStream fos = null;
            byte[] tmp = new byte[1024];
            try {
                inputStream = getApplicationContext().getAssets().open("syhlogo.png");
                fos = new FileOutputStream(file);
                int length = 0;
                while((length = inputStream.read(tmp)) > 0){
                    fos.write(tmp, 0, length);
                }
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final BroadcastReceiver printReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_NOT_CHARGING);
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                    if (status != BatteryManager.BATTERY_STATUS_CHARGING) {
                        if (level * 5 <= scale) {  //<20% 不打印
                            LowBattery = true;
                        } else {
                            LowBattery = false;
                        }
                    } else {
                        LowBattery = false;
                    }

            }

        }
    };

    /**
     * 生成条码
     *
     * @param str
     *            条码内容
     * @param type
     *            条码类型： AZTEC, CODABAR, CODE_39, CODE_93, CODE_128, DATA_MATRIX,
     *            EAN_8, EAN_13, ITF, MAXICODE, PDF_417, QR_CODE, RSS_14,
     *            RSS_EXPANDED, UPC_A, UPC_E, UPC_EAN_EXTENSION;
     * @param bmpWidth
     *            生成位图宽,宽不能大于384，不然大于打印纸宽度
     * @param bmpHeight
     *            生成位图高，8的倍数
     */

    public Bitmap CreateCode(String str, com.google.zxing.BarcodeFormat type, int bmpWidth, int bmpHeight) throws WriterException {
        Hashtable<EncodeHintType,String> mHashtable = new Hashtable<EncodeHintType,String>();
        mHashtable.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 生成二维矩阵,编码时要指定大小,不要生成了图片以后再进行缩放,以防模糊导致识别失败
        BitMatrix matrix = new MultiFormatWriter().encode(str, type, bmpWidth, bmpHeight, mHashtable);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        // 二维矩阵转为一维像素数组（一直横着排）
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                } else {
                    pixels[y * width + x] = 0xffffffff;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap,具体参考api
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
