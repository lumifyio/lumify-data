package com.altamiracorp.reddawn.ucd;

import com.altamiracorp.reddawn.model.AccumuloHelper;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRepository;
import org.apache.accumulo.core.client.RowIterator;
import org.apache.accumulo.core.client.mapreduce.AccumuloInputFormat;
import org.apache.accumulo.core.client.mapreduce.InputFormatBase;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.util.PeekingIterator;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;
import java.util.Map;

public class AccumuloSentenceInputFormat extends InputFormatBase<Text, Sentence> {
    private SentenceRepository sentenceRepository = new SentenceRepository();

    public static void init(Job job, String username, byte[] password, Authorizations authorizations, String zookeeperInstanceName, String zookeeperServerNames) {
        AccumuloInputFormat.setZooKeeperInstance(job.getConfiguration(), zookeeperInstanceName, zookeeperServerNames);
        AccumuloInputFormat.setInputInfo(job.getConfiguration(), username, password, Sentence.TABLE_NAME, authorizations);
    }

    @Override
    public RecordReader<Text, Sentence> createRecordReader(InputSplit inputSplit, TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        return new RecordReaderBase<Text, Sentence>() {
            RowIterator rowIterator;

            @Override
            public void initialize(InputSplit inSplit, TaskAttemptContext attempt) throws IOException {
                super.initialize(inSplit, attempt);
                this.rowIterator = new RowIterator(scannerIterator);
                this.currentK = new Text();
                this.currentV = null;
            }

            @Override
            public boolean nextKeyValue() throws IOException, InterruptedException {
                if (!rowIterator.hasNext()) {
                    return false;
                }
                PeekingIterator<Map.Entry<Key, Value>> it = new PeekingIterator<Map.Entry<Key, Value>>(rowIterator.next());
                this.currentV = sentenceRepository.fromRow(AccumuloHelper.accumuloRowToRow(sentenceRepository.getTableName(), it));
                this.numKeysRead = this.rowIterator.getKVCount();
                this.currentKey = new Key(this.currentV.getRowKey().toString());
                this.currentK = new Text(this.currentKey.getRow());
                return true;
            }
        };
    }
}
