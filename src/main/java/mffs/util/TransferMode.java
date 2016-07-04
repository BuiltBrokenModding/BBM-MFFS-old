package mffs.util;

/**
 * The force field transfer mode.
 */
enum TransferMode {
	equalize, distribute, drain, fill;

	private TransferMode toggle(TransferMode mode) {
		return TransferMode.values()[mode.ordinal() + 1 % TransferMode.values().length];
	}

}