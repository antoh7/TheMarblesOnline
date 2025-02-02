package ru.kbuearpov.themarblesonline;

import java.util.concurrent.TimeUnit;

public class TestClass {
    private static boolean aBoolean;
    private static final Object sync = new Object();

    public static void main(String[] args) throws InterruptedException {
        //aBoolean = true;
        //Object sync = new Object();
        Thread thread = new Thread(() -> {
            while (true) {
              //  System.out.println("working...");
            }
        }, "a");
      //  thread.setDaemon(true);
        thread.start();

        System.out.println(Thread.currentThread().getName());
        Thread thread1 = new Thread(() -> {
           synchronized (thread) {
               try {
                   thread.wait();
               } catch (InterruptedException e) {
                   throw new RuntimeException(e);
               }
           }
        });
        thread1.start();
        thread.notify();
        System.out.println(thread.getState());
        TimeUnit.SECONDS.sleep(2);
        //aBoolean = false;
        System.out.println(thread.getState());
        thread.stop();

    }

}



    /*
    @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*++++=====+***++*%@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@+====++++++++====++++++++++++@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%++++=++++++++++++++==========++++++@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*+++++++++++++**+++++++++++++===++++=====+@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*+++++++++++++**#**++++++++++++++**+++++===+++++#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@#*+++++++++++++++*#%##******###******+***+++======+===@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*+++++++++++++++++++*#%#**####%%%#####****+++++++++++====+%@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*++++++++++++++=++++***#***##%%%%%####**************++=====+%@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@#***++++++++++====++********###%%%%%%%#############**++++++==+#@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@##***++++++++=====+++++++*****#######%####%%%%%%%##******++++++@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@%####**+++++++++======+++++++********##############*****++++++++*@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@%%%%##**++++=+++++===+++++++++++++++****#######****+++++++++++++@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@%%%%%%%##***++++++++++++++++++======+++**********+++++=+++++++++@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%%##*+++++++++++========-----::::--====+++++++++=++++++++*@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@%%@@@@%%#*+++=========------::::::::.......::::::::--=====++++***@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@%@@@@@%%*+=====------:::::::::::................::::::-----=+****@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@%@@@@@%*+==--------:::::::::::::...................::::::::-=+**+@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%*+====-----::::::::::::::......................::::::::=+++@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%*+==-=------:::::::::::::::.......................:::::::-++%@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%#++=--=----::::::::::::::::..........................::::::-+#@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%#*++=------:::::::::::::::.............................:::::-++@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@%#*++===-----:::::::::::::::..............................:::::=+@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@%%%%#*++===----:::::::::::::::..............................::::::=+@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@%%%#++====----::::::::::::::...............................::::::=+@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@%#++====-----::::::::::::::.............................:::::::-=#@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@%%#++=====----::::::::::::::::...........................::::::::=+@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@%%#++=====---::::::::::::::::.............................:::::::=+@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@%%%*++=====---::::::::::::::::::::::.............:::::::::::::::::=+#@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@##%%#*+=========-------===-----::::::::::::::::::::--:::::::::::::::-+*+:..=@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@%+==+**###*+==--==============+++++++==--:::::::::--==++==--:::::::::::::-=:....:%@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@++==--=**##*+=---=========--::::--==+++++==:::::::-=====-:::::::::::::::::::..::--@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@%+++++====+**==----=========-:::::--=++++===::...::-======-:::::::::::::...:::::--:%@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@++++++++===**=------====++++++++++++++++===-::....::-===++***+==---:::.....:::::::.*@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@+=+++++++++++==--::--====++++*****+====---::::.....:::::----=-:::::::......::-:::::#@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@++++++++**+++=-::::::---=======--:::::::::::::..........::::::::::.......:::--:::+@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@+====++****++=-:::::::::::-::::::::.::::::::::...........:::.............:::--:.:@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@*+===+++++++==--::::::::::::::::::..::::::::::......:....................:::::::@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@+===+++==++==--:::::::::::::::.....:::---::::......:::...................::::.*@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@+======++++==--:::::::::.........::::-=-:::.......:::..................:::::-@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@+====+++++==--:::::::::.........:::-==--::.......::::.................::::-@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@+====++++===--::::::::.........:::-====--::...::::::::...............::::=@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@+===++++===---::::::::......:::::-==++++==------:::::..............::::-#@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@+===+++====---::::::::.......:::=++*****++==+++-:..::::...........::::-@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@===++++===---:::::::::::::..:::-=+++++++=====-:.................:::::=@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@+=+++++===----::::::::::....::-==========-::::................:::::-@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@*+++++===----:::::::::::::::-=======---:::::::..............:::::=@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@+++++====-----:::::::::::::--------::...:::::::::.......::::::::+@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%++++======---:::::::::::::---=====---==--:::::::::::.:::::::::-@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@++==+======---:::::::--==+++++++****+++++=====-:::::::::::::::=@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%++++++=====--:::::--=====----::::::::::::::::--:::::::::::::=@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*++++++=====-::::::::::::::-::-------:::::::..:::::::::::::-*@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@+++++++=====---:::::::::--------====-::::::::::::::::::::-+@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@+*++++++======----:::::--------===--::::::::::::::::::--+@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@#+++++++++=======--:::::::::::::::::::::::::::--:::--=%@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*++++++++++=======-::::::::::::::::::::::-----=-=-=@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@#+++++++++++++====-:::::::::::::::::::--=======+@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*******+++++++=---:::::---:::::::--====++@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%#*******+++++==-------------===++%@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%********+++=======++++++++%@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%#****+++++++++++%@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
   */

