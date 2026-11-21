"""Make neutral armor textures so dye hues remain recognizable on every material."""
from pathlib import Path
from zipfile import ZipFile
from io import BytesIO
import argparse
import json
from PIL import Image, ImageOps

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/dyeworkshop'
MATERIALS=('leather','copper','chainmail','iron','gold','diamond','netherite','turtle_scute')

def write(path,value):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(value,indent=2)+'\n')

def neutral(archive,source,target):
    with Image.open(BytesIO(archive.read(source))) as original:
        rgba=original.convert('RGBA')
        gray=ImageOps.grayscale(rgba).convert('RGBA')
        gray.putalpha(rgba.getchannel('A'))
        target.parent.mkdir(parents=True,exist_ok=True)
        gray.save(target)

def generate(vanilla):
    with ZipFile(vanilla) as archive:
        for material in MATERIALS:
            equipment=json.loads(archive.read(f'assets/minecraft/equipment/{material}.json'))
            for layer_type,layers in equipment['layers'].items():
                for layer in layers:
                    if 'overlay' in layer['texture']:continue
                    namespace,path=layer['texture'].split(':',1)
                    source=f'assets/{namespace}/textures/entity/equipment/{layer_type}/{path}.png'
                    neutral(archive,source,ASSETS/f'textures/entity/equipment/{layer_type}/{path}.png')
                    layer['texture']='dyeworkshop:'+path
                    layer['dyeable']={'color_when_undyed':-1}
            write(ASSETS/f'equipment/{material}.json',equipment)
        def models(value,item):
            if isinstance(value,dict):
                if value.get('type')=='minecraft:model':
                    source=value['model']
                    _,path=source.split(':',1)
                    write(ASSETS/'models'/f'{path}.json',{'parent':source,
                        'textures':{'layer0':'dyeworkshop:item/'+item}})
                    value['model']='dyeworkshop:'+path
                    value['tints']=[{'type':'minecraft:dye','default':-1}]
                for child in value.values():models(child,item)
            elif isinstance(value,list):
                for child in value:models(child,item)
        for name in archive.namelist():
            if not name.startswith('assets/minecraft/items/') or not name.endswith(('_helmet.json','_chestplate.json','_leggings.json','_boots.json')):continue
            filename=name.rsplit('/',1)[1];item=filename[:-5];material=item.rsplit('_',1)[0]
            if material not in MATERIALS and material!='golden' and item!='turtle_helmet':continue
            neutral(archive,f'assets/minecraft/textures/item/{item}.png',ASSETS/f'textures/item/{item}.png')
            definition=json.loads(archive.read(name));models(definition,item)
            write(ASSETS/'items'/filename,definition)
    print('Generated neutral equipment and item layers, retaining trim overlays.')

if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('minecraft_client_jar',type=Path)
    generate(parser.parse_args().minecraft_client_jar)
