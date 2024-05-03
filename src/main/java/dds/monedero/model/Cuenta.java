package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Cuenta {

  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {}

  public Cuenta(double montoInicial) {
    //No puedo utilizar this.poner ya que obligaría a que el monto recibido sea positivo
    this.movimientos.add(new Movimiento(LocalDate.now(), montoInicial));
  }

  /*
    En mi opinión este método no debería existir ya que rompe el encapsulamiento pero como no
    conozco el dominio, no puedo estár seguro que no existe una razon válida para su existencia.
  */
  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public void poner(double cuanto) {
    if (cuanto <= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }

    if (getDepositosRealizados().count() >= 3) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }

    movimientos.add(new Movimiento(LocalDate.now(), cuanto));
  }

  public void sacar(double cuanto) {
    if (cuanto <= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }
    if (getSaldo() - cuanto < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    double limite = 1000 - montoExtraidoHoy;
    if (cuanto > limite) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
          + " diarios, límite: " + limite);
    }
    movimientos.add(new Movimiento(LocalDate.now(), -1 * cuanto));
  }

  public void agregarMovimiento(Movimiento movimiento) {
    movimientos.add(movimiento);
  }

  public double calcularSumaMovimientos(Stream<Movimiento> movimientos){
    return movimientos.mapToDouble(Movimiento::getMonto).sum();
  }

  public double getMontoExtraidoA(LocalDate fecha) {
    return calcularSumaMovimientos(getExtraccionesA(fecha));
  }

  public double getSaldo() {
    return calcularSumaMovimientos(movimientos.stream());
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  private Stream<Movimiento> getDepositosRealizados(){
    return getMovimientos().stream().filter(Movimiento::isDeposito);
  }

  private Stream<Movimiento> getExtraccionesA(LocalDate fecha){
    return getMovimientos().stream().filter(movimiento -> movimiento.fueExtraido(fecha));
  }
}
