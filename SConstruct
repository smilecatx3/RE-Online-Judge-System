import configparser
import glob
import os


config_parser = configparser.ConfigParser()
config_parser.read('config.ini')
config = config_parser['DEFAULT']


## [STEP-1] Compile
classes_dir = os.path.join('webapp', 'WEB-INF', 'classes')
libs = list(x for x in glob.glob(os.path.join(config['lib_dir'], '*.jar')))

env_java = Environment(
    JAVAC=File(config['javac']),
    JAVACFLAGS=['-encoding', 'utf8'],
    JAVACLASSPATH=libs+[classes_dir],
    JAVACCOMSTR='Compiling...'
)
java_build = env_java.Java(classes_dir, [Dir('system/src'), Dir('webapp/WEB-INF/src')])


## [STEP-2] Make final product (servlet war file)
def make_final_product(target, source, env):
    product_name = str(target[0])+'.war'
    source_dir = str(source[0])
    print('Making final product: "{0}"'.format(product_name))
    for (dirpath, dirnames, filenames) in os.walk(source_dir):
        for x in filenames:
            env.Zip(product_name, File(os.path.join(dirpath, x)))


env_war = Environment(ZIPROOT='webapp')
env_war['PRINT_CMD_LINE_FUNC'] = lambda s, targets, sources, env: None
env_war['BUILDERS']['Build'] = Builder(action=make_final_product)

final_product = env_war.Build(config['appname'], Dir('webapp'))
AlwaysBuild(final_product)
Depends(final_product, java_build)
