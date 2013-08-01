package com.altamiracorp.reddawn.vaast;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.vaast.model.subFrames.AccumuloSubFrameInputFormat;
import com.altamiracorp.reddawn.vaast.model.subFrames.SubFrame;
import com.altamiracorp.reddawn.vaast.model.subFrames.SubFrameRepository;
import com.altamiracorp.vaast.core.measurement.GaussianMatrix;
import com.altamiracorp.vaast.core.measurement.Matrix;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SparsifyMR extends ConfigurableMapJobBase {


    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        AccumuloSubFrameInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloSubFrameInputFormat.class;
    }

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        SparsifyMapper.init(job, clazz);
        return SparsifyMapper.class;
    }

    public static class SparsifyMapper extends Mapper<Text, SubFrame, Text, SubFrame> {
        private static final String MATRIX_CLASS = "matrixClass";
        private static final String MATRIX_FILE_PATH = "matrixFilePath";
        private static final String MATRIX_FILE_PATH_DEFAULT = "/conf/vaast/matrix";

        private RedDawnSession session;
        private SubFrameRepository subFrameRepository = new SubFrameRepository();
        private FileSystem fs;
        private Matrix matrix;

        @Override
        public void setup(Context context) throws IOException {
            try {
                session = RedDawnSession.create(context);
                FileSystem fs = FileSystem.get(context.getConfiguration());
                Path matrixPath = new Path(context.getConfiguration().get(MATRIX_FILE_PATH, MATRIX_FILE_PATH_DEFAULT));
                InputStream matrixIn = fs.open(matrixPath);
                matrix = (Matrix) context.getConfiguration().getClass(MATRIX_CLASS, GaussianMatrix.class).newInstance();
                matrix.load(matrixIn, new BaseConfiguration());
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        public void map(Text rowKey, SubFrame subFrame, Context context) throws IOException{
            try {
                byte[] sparseBytes = matrix.generateSparse(subFrameRepository.getRaw(session.getModelSession(),subFrame));
                subFrameRepository.saveSparseSubFrame(session.getModelSession(),subFrame,new ByteArrayInputStream(sparseBytes));
                context.write(new Text(SubFrame.TABLE_NAME),subFrame);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        public static void init(Job job, Class<? extends Matrix> matrixClass) {
            job.getConfiguration().setClass(MATRIX_CLASS, matrixClass, Matrix.class);
        }

    }
}
