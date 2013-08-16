package com.altamiracorp.reddawn.vaast;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.AccumuloSession;
import com.altamiracorp.reddawn.vaast.model.averageFrames.AccumuloAverageFrameInputFormat;
import com.altamiracorp.reddawn.vaast.model.averageFrames.AverageFrame;
import com.altamiracorp.reddawn.vaast.model.averageFrames.AverageFrameRepository;
import com.altamiracorp.reddawn.vaast.model.subFrames.SubFrame;
import com.altamiracorp.reddawn.vaast.model.subFrames.SubFrameRepository;
import com.altamiracorp.reddawn.vaast.model.subFrames.SubFrameRowKey;
import com.altamiracorp.vaast.core.model.SubSection;
import com.altamiracorp.vaast.core.model.SubSectionedFrame;
import com.altamiracorp.vaast.core.util.Util;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.ToolRunner;
import org.apache.pdfbox.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SubFrameMR extends ConfigurableMapJobBase {

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        AccumuloAverageFrameInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloAverageFrameInputFormat.class;
    }

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        return SubFrameMapper.class;
    }

    public static class SubFrameMapper extends Mapper<Text, AverageFrame, Text, SubFrame> {
        private static final String ROWS = "rows";
        private static final String DEFAULT_ROWS = "4";
        private static final String COLUMNS = "cols";
        private static final String DEFAULT_COLUMNS = "4";

        private RedDawnSession session;
        private AverageFrameRepository averageFrameRepository = new AverageFrameRepository();
        private SubFrameRepository subFrameRepository = new SubFrameRepository();

        @Override
        public void setup(Context context) {
            session = RedDawnSession.create(context);
            session.getModelSession().initializeTable(SubFrame.TABLE_NAME);
        }

        @Override
        public void map(Text rowKey, AverageFrame frame, Context context) throws IOException {
            try {
                InputStream frameIn = averageFrameRepository.getRaw(session.getModelSession(), frame);
                byte[] frameBytes = IOUtils.toByteArray(frameIn);
                SubFrame subFrame;
                SubSectionedFrame ssf = Util.subSection(frame.getMetadata().getWidth(),
                        frame.getMetadata().getHeight(),
                        frame.getMetadata().getChannels(),
                        frameBytes,
                        Integer.parseInt(context.getConfiguration().get(COLUMNS, DEFAULT_COLUMNS)),
                        Integer.parseInt(context.getConfiguration().get(ROWS, DEFAULT_ROWS)));


                for (int i = 0; i < ssf.getSubSections().length; i++) {
                    SubSection subSection = ssf.getSubSections()[i];
                    subFrame = new SubFrame(new SubFrameRowKey(frame.getRowKey().toString(), i));
                    subFrameRepository.saveSubFrame(session.getModelSession(), subFrame.getRowKey().toString(),
                            new ByteArrayInputStream(subSection.getContent()));

                    //add metadata
                    subFrame.getMetadata().setX1(subSection.getX())
                            .setY1(subSection.getY())
                            .setX2(subSection.getX() + ssf.getWidth())
                            .setY2(subSection.getY() + ssf.getHeight());
                    context.write(new Text(SubFrame.TABLE_NAME), subFrame);
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

    }

    @Override
    protected boolean hasConfigurableClassname() {
        return false;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new SubFrameMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}
