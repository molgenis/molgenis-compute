package org.molgenis.compute5.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by georgebyelas on 16/03/15.
 */
public class BatchAnalyser
{

    private final String batchVariable;
    private final int batchSize;

    private List<Batch> batches = new ArrayList<Batch>();

    public int getBatchesSize()
    {
        return batches.size();
    }

    private class Batch
    {
        public int number = -1;
        public String[] values;

        public int filledSize = 0;

        public Batch(int num, int size)
        {
            this.number = num;
            values = new String[size];
        }

        public void addValue(String value)
        {
            values[filledSize] = value;
            filledSize++;
        }
    }

    public BatchAnalyser(String batchVariable, int batchSize)
    {
        this.batchVariable = batchVariable;
        this.batchSize = batchSize;
    }

    public int getBatchNum(Map<String, Object> map)
    {
        int batchNumber;

        String taskBatchVariableValue = (String) map.get(batchVariable);

        //first look, if variable value is already in existing batches
        batchNumber = findInBatches(taskBatchVariableValue);

        if(batchNumber == -1)
        {
            batchNumber = addToBatches(taskBatchVariableValue);
        }

        return batchNumber;
    }

    private int addToBatches(String taskBatchVariableValue)
    {
        //get the latest batch
        boolean createNewBatch = false;
        if(batches.size() > 0)
        {
            Batch batch = batches.get(batches.size() - 1);

            if (batch.filledSize != batchSize)
            {
                batch.addValue(taskBatchVariableValue);
                return batch.number;
            }
            else
                createNewBatch = true;
        }
        else
            createNewBatch = true;

        if(createNewBatch)
        {
            //create new batch
            Batch b = new Batch(batches.size(), batchSize);
            batches.add(b);
            b.values[0] = taskBatchVariableValue;
            return b.number;
        }
        return -1;
    }

    private int findInBatches(String taskBatchVariableValue)
    {
        for(Batch batch : batches)
        {
            for(int i = 0; i < batch.filledSize; i++)
            {
                if(batch.values[i].equalsIgnoreCase(taskBatchVariableValue))
                    return batch.number;
            }
        }
        return -1;
    }
}
