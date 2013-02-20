package hydraulic.core.pressure;

/**
 * An easy way to display information on electricity.
 * 
 * @author Calclavia
 */

public class FluidInfo
{
	public static enum FluidUnits
	{
		FLOW_Rate("FLOW", "I"), PRESSURE("Psi", "P"), FORCE("Force", "N"),  RESISTANCE("Ohm", "R");

		public String name;
		public String symbol;

		private FluidUnits(String name, String symbol)
		{
			this.name = name;
			this.symbol = symbol;
		}

		public String getPlural()
		{
			return this.name + "s";
		}
	}

	public static enum MeasurementUnit
	{
		MICRO("Micro", "u", 0.000001), MILLI("Milli", "m", 0.001), KILO("Kilo", "k", 1000), MEGA("Mega", "M", 1000000);

		public String name;
		public String symbol;
		public double value;

		private MeasurementUnit(String name, String symbol, double value)
		{
			this.name = name;
			this.symbol = symbol;
			this.value = value;
		}

		public String getName(boolean isSymbol)
		{
			if (isSymbol)
			{
				return symbol;
			}
			else
			{
				return name;
			}
		}

		public double process(double value)
		{
			return value / this.value;
		}
	}

	

	/**
	 * Displays the unit as text. Works only for positive numbers.
	 */
	public static String getDisplay(double value, FluidUnits unit, int decimalPlaces, boolean isShort)
	{
		String unitName = unit.name;

		if (isShort)
		{
			unitName = unit.symbol;
		}
		else if (value > 1)
		{
			unitName = unit.getPlural();
		}

		if (value == 0) { return value + " " + unitName; }

		if (value <= MeasurementUnit.MILLI.value) { return roundDecimals(MeasurementUnit.MICRO.process(value), decimalPlaces) + " " + MeasurementUnit.MICRO.getName(isShort) + unitName; }

		if (value < 1) { return roundDecimals(MeasurementUnit.MILLI.process(value), decimalPlaces) + " " + MeasurementUnit.MILLI.getName(isShort) + unitName; }

		if (value > MeasurementUnit.MEGA.value) { return roundDecimals(MeasurementUnit.MEGA.process(value), decimalPlaces) + " " + MeasurementUnit.MEGA.getName(isShort) + unitName; }

		if (value > MeasurementUnit.KILO.value) { return roundDecimals(MeasurementUnit.KILO.process(value), decimalPlaces) + " " + MeasurementUnit.KILO.getName(isShort) + unitName; }

		return roundDecimals(value, decimalPlaces) + " " + unitName;
	}

	public static String getDisplay(double value, FluidUnits unit)
	{
		return getDisplay(value, unit, 2, false);
	}

	public static String getDisplayShort(double value, FluidUnits unit)
	{
		return getDisplay(value, unit, 2, true);
	}

	public static String getDisplayShort(double value, FluidUnits unit, int decimalPlaces)
	{
		return getDisplay(value, unit, decimalPlaces, true);
	}

	public static String getDisplaySimple(double value, FluidUnits unit, int decimalPlaces)
	{
		if (value > 1)
		{
			if (decimalPlaces < 1) { return (int) value + " " + unit.getPlural(); }

			return roundDecimals(value, decimalPlaces) + " " + unit.getPlural();
		}

		if (decimalPlaces < 1) { return (int) value + " " + unit.name; }

		return roundDecimals(value, decimalPlaces) + " " + unit.name;
	}

	/**
	 * Rounds a number to a specific number place places
	 * 
	 * @param The number
	 * @return The rounded number
	 */
	public static double roundDecimals(double d, int decimalPlaces)
	{
		int j = (int) (d * Math.pow(10, decimalPlaces));
		return j / (double) Math.pow(10, decimalPlaces);
	}

	public static double roundDecimals(double d)
	{
		return roundDecimals(d, 2);
	}
}
