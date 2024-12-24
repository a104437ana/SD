import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Menu {

    private static Scanner in = new Scanner(System.in);
    private static boolean windows;

    public interface Handler {
        public void execute();
    }

    public interface PreCondition {
        public boolean validate();
    }

    private String name;
    private List<String> options;
    private List<PreCondition> preConditions;
    private List<Handler> handlers;

    public Menu (String name, String[] ops) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) Menu.windows = true;
        else Menu.windows = false;
        this.name = name;
        this.options = Arrays.asList(ops);
        this.preConditions = new ArrayList<>();
        this.handlers = new ArrayList<>();
        for (String s : options) {
            this.preConditions.add(() -> true);
            this.handlers.add(() -> {System.out.println("\nOpcao nao implementada"); pressEnterToContinue();});
        }
    }

    public void setHandler(int i, Handler h) {
        this.handlers.set(i-1, h);
    }

    public void setPreCondition(int i, PreCondition p) {
        this.preConditions.set(i-1, p);
    }

    public void run() {
        int op;
        do {
            clearTerminal();
            show();
            op = readOption();
            if (op > 0 && !preConditions.get(op-1).validate()) {
                System.out.println("Opcao indisponivel");
                pressEnterToContinue();
            }
            else if (op > 0) {
                handlers.get(op-1).execute();
            }
        }
        while (op != 0);
    }

    public void show() {
        System.out.println("\n --- " + name + " --- ");
        for (int i = 0; i < options.size(); i++) {
            System.out.print(i+1);
            System.out.print(" - ");
            String option = preConditions.get(i).validate() ? options.get(i) : "---";
            System.out.println(option);
        }
        System.out.println("0 - Sair");
    }

    public int readOption() {
        int op;

        System.out.print("Opcao : ");
        try {
            while (!in.hasNextLine()) {}
            String line = in.nextLine();
            op = Integer.parseInt(line);
        }
        catch (NumberFormatException e) {
            op = -1;
        }
        if (op < 0 || op > options.size()) {
            System.out.println("Opcao invalida");
            op = -1;
            pressEnterToContinue();
        }
        return op;
    }

    public static void clearTerminal () {
        ProcessBuilder builder;
        if (!windows) builder = new ProcessBuilder("clear");
        else builder = new ProcessBuilder("cmd", "/c", "cls");
        builder.inheritIO();
        try {
            builder.start().waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void pressEnterToContinue() { 
        System.out.print("Prima a tecla Enter para continuar...");
        try {
            in.nextLine();
        }  
        catch(Exception e) {
        }  
    }
}
