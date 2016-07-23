package mffs.base;

import com.builtbroken.mc.api.process.IThreadProcess;

/**
 * Process task used to thread forcefield calculations
 */
public class FieldCalculationTask implements IThreadProcess
{
    public final TileFieldMatrix matrix;

    public FieldCalculationTask(TileFieldMatrix matrix)
    {
        this.matrix = matrix;
    }

    @Override
    public void runProcess()
    {
        matrix.generateCalculatedField();
        matrix.isCalculating = false;
    }

    @Override
    public void killAction()
    {
        matrix.isCalculating = false;
        //TODO error?
    }
}