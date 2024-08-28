public class Test {

    public static void main(String[] args) {

        byte b = 0;

        b |= 1;
        System.out.println(b);

//        b |= 2;
//        System.out.println(b);

        b |= 4;
        System.out.println(b);

        b |= 8;
        System.out.println(b);

        System.out.println(b & 2);
    }
}
