import java.util.*;
import java.util.concurrent.*;

public class TorneoTenis {

    record Jugador(int numero) {
        @Override
        public String toString() {
            return "Jugador " + numero;
        }
    }

    record ResultadoPartido(Jugador jugador1, Jugador jugador2, List<Jugador> setsGanados, Jugador ganador) {}

    public static void main(String[] args) throws Exception {
        List<Jugador> jugadores = new ArrayList<>();
        for (int i = 1; i <= 16; i++) {
            jugadores.add(new Jugador(i));
        }

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        String[] rondas = { "OCTAVOS DE FINAL", "CUARTOS DE FINAL", "SEMIFINAL", "FINAL" };
        int rondaIndex = 0;

        while (jugadores.size() > 1) {
            System.out.println("===== " + rondas[rondaIndex++] + " =====");
            List<Future<ResultadoPartido>> futuros = new ArrayList<>();
            List<Jugador> siguientesRonda = new ArrayList<>();

            for (int i = 0; i < jugadores.size() / 2; i++) {
                Jugador jugador1, jugador2;
                if (jugadores.size() == 16) {
                    jugador1 = jugadores.get(i);
                    jugador2 = jugadores.get(jugadores.size() - 1 - i);
                } else {
                    jugador1 = jugadores.get(2 * i);
                    jugador2 = jugadores.get(2 * i + 1);
                }

                Jugador finalJugador1 = jugador1;
                Jugador finalJugador2 = jugador2;

                Callable<ResultadoPartido> partido = () -> simularPartido(finalJugador1, finalJugador2);
                futuros.add(executor.submit(partido));
            }

            for (Future<ResultadoPartido> futuro : futuros) {
                ResultadoPartido resultado = futuro.get();
                imprimirResultado(resultado);
                siguientesRonda.add(resultado.ganador());
            }

            jugadores = siguientesRonda;
        }

        System.out.println("\n🏆 ¡Campeón del torneo: " + jugadores.get(0) + "!");

        executor.shutdown();
    }

    private static ResultadoPartido simularPartido(Jugador jugador1, Jugador jugador2) throws InterruptedException {
        Random rand = new Random();
        List<Jugador> setsGanados = new ArrayList<>();
        int victoriasJ1 = 0, victoriasJ2 = 0;

        for (int i = 1; i <= 3 && victoriasJ1 < 2 && victoriasJ2 < 2; i++) {
            Thread.sleep(1500 + rand.nextInt(501)); 

            Jugador ganadorSet = rand.nextBoolean() ? jugador1 : jugador2;
            setsGanados.add(ganadorSet);

            if (ganadorSet.equals(jugador1)) victoriasJ1++;
            else victoriasJ2++;
        }

        Jugador ganador = victoriasJ1 > victoriasJ2 ? jugador1 : jugador2;
        return new ResultadoPartido(jugador1, jugador2, setsGanados, ganador);
    }

    private static void imprimirResultado(ResultadoPartido resultado) {
        System.out.println(resultado.jugador1() + " vs " + resultado.jugador2());
        for (int i = 0; i < resultado.setsGanados().size(); i++) {
            System.out.println("Set " + (i + 1) + ": " + resultado.setsGanados().get(i));
        }
        System.out.println("Ganador del partido: " + resultado.ganador() + "\n");
    }
}
