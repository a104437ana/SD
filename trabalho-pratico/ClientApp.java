import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class ClientApp {

    private Map<String,Menu> menus;
    private Client client;
    private boolean AUTHENTICATED;
    private Scanner in;
    
    public ClientApp(boolean singleThread) {
        try {
            if (singleThread) this.client = new ClientSingleThread();
            else this.client = new ClientMultiThread();
        }
        catch (IOException e) {
            e.printStackTrace();
            Menu.pressEnterToContinue();
        }
        this.AUTHENTICATED = false;
        this.menus = new HashMap<>();
        this.in = new Scanner(System.in);
    }

    private void menuInicial() {
        Menu menuInicial = menus.get("inicial");
        if (menuInicial == null) {
            String[] menuInicialOps = new String[] {"Autenticar", "Registar"};
            menuInicial = new Menu("Menu Inicial", menuInicialOps);
            menuInicial.setHandler(1, () -> menuAutenticar());
            menuInicial.setHandler(2, () -> menuRegistar());
            this.menus.put("inicial", menuInicial);
        }
        menuInicial.run();
    }

    private void menuAutenticar() {
        Menu.clearTerminal();
        System.out.println("\n --- Menu Autenticar --- ");
        System.out.print("Username : ");
        String userName = in.nextLine();
        System.out.print("Password : ");
        String password = in.nextLine();
//        AUTHENTICATED = true;
        AUTHENTICATED = client.authenticate(userName, password);
        if (AUTHENTICATED) {
            String[] menuInicialOps = new String[] {"Get", "Put", "MultiGet", "MultiPut", "GetWhen"};
            Menu menuInicial = new Menu("Menu Inicial - Autenticado", menuInicialOps);
            menuInicial.setHandler(1, () -> menuGet());
            menuInicial.setHandler(2, () -> menuPut());
            menuInicial.setHandler(3, () -> menuMultiGet());
            menuInicial.setHandler(4, () -> menuMultiPut());
//            menuInicial.setHandler(5, () -> menuGetWhen());
            this.menus.put("inicial", menuInicial);
            System.out.println("Autenticado com sucesso");
            Menu.pressEnterToContinue();
            menuInicial();
        }
        else {
            System.out.println("Autenticação falhou");
            Menu.pressEnterToContinue();
        }
    }

    private void menuRegistar() {
        Menu.clearTerminal();
        System.out.println("\n --- Menu Registar --- ");
        System.out.print("Username : ");
        String userName = in.nextLine();
        System.out.print("Password : ");
        String password = in.nextLine();
//        boolean registed = true;
        boolean registed = client.register(userName, password);
        if (registed) {
            System.out.println("Registado com sucesso");
            Menu.pressEnterToContinue();
        }
        else {
            System.out.println("Registo Falhou");
            Menu.pressEnterToContinue();
        }
    }

    private void menuGet() {
        Menu.clearTerminal();
        System.out.println("\n --- Menu Get --- ");
        System.out.print("Get key : ");
        String key = in.nextLine();
//        byte[] value = new byte[10];
        byte[] value = client.get(key);
        // Falta alterar para mostrar o value, para ficheiro ou no ecrã
        if (value != null) {
            System.out.println("Get efetuado com sucesso");
            Menu.pressEnterToContinue();
        }
        else {
            System.out.println("Get Falhou");
            Menu.pressEnterToContinue();
        }
    }

    private void menuPut() {
        Menu.clearTerminal();
        System.out.println("\n --- Menu Put --- ");
        System.out.print("Put key : ");
        String key = in.nextLine();
        System.out.print("Put value : ");
        // Talvez alterar para ter a opção de enviar um ficheiro
        byte[] value = in.nextLine().getBytes();
        client.put(key, value);
        System.out.println("Put efetuado com sucesso");
        Menu.pressEnterToContinue();
    }

    private void menuMultiGet() {
        int numberOfPairs = -1;
        do {
            Menu.clearTerminal();
            System.out.println("\n --- Menu MultiGet --- ");
            System.out.print("Número de pares : ");
            String numPairs = in.nextLine();
            try {
                numberOfPairs = Integer.parseInt(numPairs);
            }
            catch (NumberFormatException e) {
                System.out.println("Número de pares inválido");
                numberOfPairs = -1;
                Menu.pressEnterToContinue();
            }
        }
        while (numberOfPairs == -1);
        Set<String> keys = new HashSet<>();
        Map<String,byte[]> pairs = new HashMap<>(); // Depois retirar, só para poder testar
        for (int i = 0; i < numberOfPairs; i++) {
            boolean exists = true;
            do {
                System.out.print("Get " + (i+1) + " key : ");
                String key = in.nextLine();
                exists = keys.contains(key);
                if (exists) {
                    System.out.println("Chave já foi introduzida");
                    System.out.println("Introduza uma nova chave");
                    Menu.pressEnterToContinue();
                }
                else {
                    keys.add(key);
                    pairs.put(key,new byte[10]); // Depois retirar, só para poder testar
                }
            }
            while (exists);
        }
//        Map<String,byte[]> pairs = client.multiGet(keys);
        if (pairs.size() == numberOfPairs) {
            System.out.println("MultiGet efetuado com sucesso");
            Menu.pressEnterToContinue();
        }
        else {
            System.out.println("MultiGet Falhou");
            Menu.pressEnterToContinue();
        }
    }

    private void menuMultiPut() {
        int numberOfPairs = -1;
        do {
            Menu.clearTerminal();
            System.out.println("\n --- Menu MultiPut --- ");
            System.out.print("Número de pares : ");
            String numPairs = in.nextLine();
            try {
                numberOfPairs = Integer.parseInt(numPairs);
            }
            catch (NumberFormatException e) {
                System.out.println("Número de pares inválido");
                numberOfPairs = -1;
                Menu.pressEnterToContinue();
            }
        }
        while (numberOfPairs == -1);
        Map<String,byte[]> pairs = new HashMap<>();
        for (int i = 0; i < numberOfPairs; i++) {
            System.out.print("Put " + (i+1) + " key : ");
            String key = in.nextLine();
            System.out.print("Put " + (i+1) + " value : ");
            byte[] value = in.nextLine().getBytes();
            pairs.put(key,value);
        }
//        client.multiPut(pairs);
        System.out.println("MultiPut efetuado com sucesso");
        Menu.pressEnterToContinue();
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            String arg = args[0];
            boolean singleThread = true;
            if (arg.equals("-s")) {
                singleThread = true;
                ClientApp clientApp = new ClientApp(singleThread);
                clientApp.menuInicial();
            }
            else if (arg.equals("-m")) {
                singleThread = false;
                ClientApp clientApp = new ClientApp(singleThread);
                clientApp.menuInicial();
            }
            else {
                System.out.println("Argumento inválido");
                System.out.println("  -s : para cliente single thread");
                System.out.println("  -m : para cliente multi thread");
            }
        }
        else {
            System.out.println("Número de argumentos inválido");
            System.out.println("  java ClientApp <argumento>");
            System.out.println("    -s : para cliente single thread");
            System.out.println("    -m : para cliente multi thread");
        }
    }
}
