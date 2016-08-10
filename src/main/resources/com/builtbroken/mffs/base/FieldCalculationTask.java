package com.builtbroken.mffs.base;

import com.builtbroken.mc.api.process.IProcessListener;
import com.builtbroken.mc.api.process.IThreadProcess;
import com.builtbroken.mffs.api.machine.IFieldMatrix;

/**
 * Process task used to thread forcefield calculations
 */
public class FieldCalculationTask implements IThreadProcess
{
    public final IFieldMatrix matrix;
    public final IProcessListener listener;

    public FieldCalculationTask(IFieldMatrix matrix, IProcessListener listener)
    {
        this.matrix = matrix;
        this.listener = listener;
    }

    @Override
    public void runProcess()
    {
        listener.onProcessStarts(this);
        try
        {
            matrix.setCalculatedField(matrix.generateCalculatedField());
            listener.onProcessFinished(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            listener.onProcessTerminated(this);
        }
    }

    @Override
    public void killAction()
    {
        listener.onProcessTerminated(this);
        //TODO error?
    }
}