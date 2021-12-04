import java.io.*;

    interface filter {                              // creating the filter template
        double[] process(double[] data);            // each template to have a process method to do the actual filtering
    }

    class pipe{                                     // pipe for temperatures
        double[] data;                              // temperature data
        int index;                                  // index for number of filters
        filter[] filters;                           // array for storing filters

        pipe(int n,int m){                          // initialize with number of filters and number of temperatures
            data=new double[n];
            filters = new filter[m];
            index=0;
        }

        pipe(int m,double...temps){                 // initialize with number of filters and temperatures
            data=temps;
            filters = new filter[m];
            index=0;
        }

        public void addFilter(filter f) {           // add a filter to the pipe
            filters[index++]=f;
        }

        public void run(){                          // execute each filter in order
            for (int i=0;i<index;i++) {
                data = filters[i].process(data);
            }
        }
    }

    class run {
        public static void main(String[] args) {
            pipe P= new pipe(4,4.5, 12.7, 11, 18.9,7.90, 5.32);                 // create filter
            filter top = (data) -> {                                            // filter for removing top value
                int index=0;
                double[] newData= new double[data.length-1];
                for (int i = 0; i < data.length; i++) {                         // find top value
                    if(data[index]<data[i])
                        index=i;
                }
                for (int i = 0; i < data.length; i++) {                         // copy all values except top value into a new array
                    if(i!=index)
                        newData[ i>index ? i-1:i]= data[i];
                }
                return newData;                                                 // return new array
            };
            filter bottom = (data) -> {                                         // filter for removing bottom value
                int index=0;
                double[] newData= new double[data.length-1];
                for (int i = 0; i < data.length; i++) {                         // find bottom value
                    if(data[index]>data[i])
                        index=i;
                }
                for (int i = 0; i < data.length; i++) {                         // copy all values except bottom value into a new array
                    if(i!=index)
                        newData[ i>index ? i-1:i]= data[i];
                }
                return newData;                                                 // return new array
            };
            filter transform = (data) ->{                                       // filter for transforming celsius to fahrenheit
                for (int i = 0; i < data.length; i++) {
                    data[i]= (data[i]*9/5)+32;
                }
                return data;                                                    // return transformed data
            };
            filter print = (data) ->{                                           // filter for printing values
                for (double d : data) {
                    System.out.println(d);                                      // print each value
                }
                return data;                                                    // return data so that it fits interface pattern
            };
            // add filters to pipe
            P.addFilter(top);
            P.addFilter(bottom);
            P.addFilter(transform);
            P.addFilter(print);
            P.run();                                                            // run the pipe
        }
    }