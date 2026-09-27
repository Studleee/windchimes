# Regenerates the JSON files for every wind chime listed below.
# Usage: .\tools\gen-chime-assets.ps1
# Keep this list in sync with ModBlocks.java. Recipes are only written if missing, so hand edits survive.
$ErrorActionPreference = 'Stop'
trap { Write-Host "Error: $_"; exit 1 }

# material, display name, rod texture, cap texture, recipe slab, recipe material
$chimes = @(
	@('bamboo',   'Bamboo',   'bamboo_block',   'bamboo_planks',   $null, $null),
	@('iron',     'Iron',     'iron_block',     'spruce_planks',   $null, $null),
	@('copper',   'Copper',   'copper_block',   'dark_oak_planks', $null, $null),
	@('gold',     'Gold',     'gold_block',     'dark_oak_planks', $null, $null),
	@('amethyst', 'Amethyst', 'amethyst_block', 'cherry_planks',   $null, $null)
)

$root = Join-Path $PSScriptRoot '..\src\main\resources'
$assets = Join-Path $root 'assets\windchimes'
$data = Join-Path $root 'data\windchimes'
$utf8 = New-Object System.Text.UTF8Encoding($false)

function Write-Json($path, $text) {
	$dir = Split-Path $path
	if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
	[System.IO.File]::WriteAllText($path, ($text.Trim() -replace "`r`n", "`n") + "`n", $utf8)
}

# Puts a recipe in the recipe book once the player picks up the given item.
function Write-Advancement($id, $unlockItem) {
	Write-Json (Join-Path $data "advancement\recipes\decorations\$id.json") @"
{
	"parent": "minecraft:recipes/root",
	"criteria": {
		"has_ingredient": {
			"conditions": {
				"items": [
					{
						"items": "$unlockItem"
					}
				]
			},
			"trigger": "minecraft:inventory_changed"
		},
		"has_the_recipe": {
			"conditions": {
				"recipes": "windchimes:$id"
			},
			"trigger": "minecraft:recipe_unlocked"
		}
	},
	"requirements": [
		[
			"has_the_recipe",
			"has_ingredient"
		]
	],
	"rewards": {
		"recipes": [
			"windchimes:$id"
		]
	}
}
"@
}

$langPath = Join-Path $assets 'lang\en_us.json'
$lang = [ordered]@{}
(Get-Content $langPath -Raw | ConvertFrom-Json).PSObject.Properties | ForEach-Object { $lang[$_.Name] = $_.Value }

foreach ($c in $chimes) {
	$material, $name, $rod, $cap, $slab, $recipeMaterial = $c
	$id = "${material}_wind_chime"

	# Full model: used for the item and as a reference. The placed block only shows the cap;
	# the rods and strings are drawn by WindChimeRenderer so they can swing.
	Write-Json (Join-Path $assets "models\block\$id.json") @"
{
	"parent": "windchimes:block/wind_chime",
	"textures": {
		"rod": "minecraft:block/$rod",
		"top": "minecraft:block/$cap",
		"string": "minecraft:block/white_wool"
	}
}
"@

	Write-Json (Join-Path $assets "models\block\${id}_cap.json") @"
{
	"parent": "windchimes:block/wind_chime_cap",
	"textures": {
		"rod": "minecraft:block/$rod",
		"top": "minecraft:block/$cap"
	}
}
"@

	Write-Json (Join-Path $assets "blockstates\$id.json") @"
{
	"variants": {
		"muffled=false": { "model": "windchimes:block/${id}_cap" },
		"muffled=true": { "model": "windchimes:block/${id}_cap" }
	}
}
"@

	Write-Json (Join-Path $assets "items\$id.json") @"
{
	"model": {
		"type": "minecraft:model",
		"model": "windchimes:block/$id"
	}
}
"@

	Write-Json (Join-Path $data "loot_table\blocks\$id.json") @"
{
	"type": "minecraft:block",
	"pools": [
		{
			"rolls": 1.0,
			"entries": [
				{
					"type": "minecraft:item",
					"name": "windchimes:$id"
				}
			],
			"conditions": [
				{
					"condition": "minecraft:survives_explosion"
				}
			]
		}
	]
}
"@

	$recipePath = Join-Path $data "recipe\$id.json"
	if ($slab -and -not (Test-Path $recipePath)) {
		Write-Json $recipePath @"
{
	"type": "minecraft:crafting_shaped",
	"category": "misc",
	"group": "wind_chime",
	"pattern": [
		" W ",
		"SSS",
		"MMM"
	],
	"key": {
		"W": "$slab",
		"S": "minecraft:string",
		"M": "$recipeMaterial"
	},
	"result": {
		"id": "windchimes:$id",
		"count": 1
	}
}
"@
	}

	Write-Advancement $id 'minecraft:string'

	$lang["block.windchimes.$id"] = "$name Wind Chime"
}

# Long chimes: two blocks tall. Crafted from the normal chime with more of its material underneath.
# material, display name, rod texture, cap texture, recipe material
$longChimes = @(
	@('bamboo',   'Bamboo',   'bamboo_block',   'bamboo_planks',   'minecraft:bamboo'),
	@('iron',     'Iron',     'iron_block',     'spruce_planks',   'minecraft:iron_nugget'),
	@('copper',   'Copper',   'copper_block',   'dark_oak_planks', 'minecraft:copper_nugget'),
	@('gold',     'Gold',     'gold_block',     'dark_oak_planks', 'minecraft:gold_nugget'),
	@('amethyst', 'Amethyst', 'amethyst_block', 'cherry_planks',   'minecraft:amethyst_shard')
)

foreach ($c in $longChimes) {
	$material, $name, $rod, $cap, $recipeMaterial = $c
	$id = "long_${material}_wind_chime"
	$shortId = "${material}_wind_chime"

	Write-Json (Join-Path $assets "models\block\$id.json") @"
{
	"parent": "windchimes:block/long_wind_chime",
	"textures": {
		"rod": "minecraft:block/$rod",
		"top": "minecraft:block/$cap",
		"string": "minecraft:block/white_wool"
	}
}
"@

	Write-Json (Join-Path $assets "models\block\${id}_cap.json") @"
{
	"parent": "windchimes:block/wind_chime_cap",
	"textures": {
		"rod": "minecraft:block/$rod",
		"top": "minecraft:block/$cap"
	}
}
"@

	# The lower half draws nothing itself; the upper half's renderer draws the rods hanging into it.
	Write-Json (Join-Path $assets "models\block\${id}_lower.json") @"
{
	"textures": {
		"particle": "minecraft:block/$rod"
	}
}
"@

	Write-Json (Join-Path $assets "blockstates\$id.json") @"
{
	"variants": {
		"half=upper,muffled=false": { "model": "windchimes:block/${id}_cap" },
		"half=upper,muffled=true": { "model": "windchimes:block/${id}_cap" },
		"half=lower,muffled=false": { "model": "windchimes:block/${id}_lower" },
		"half=lower,muffled=true": { "model": "windchimes:block/${id}_lower" }
	}
}
"@

	Write-Json (Join-Path $assets "items\$id.json") @"
{
	"model": {
		"type": "minecraft:model",
		"model": "windchimes:block/$id"
	}
}
"@

	Write-Json (Join-Path $data "loot_table\blocks\$id.json") @"
{
	"type": "minecraft:block",
	"pools": [
		{
			"rolls": 1.0,
			"entries": [
				{
					"type": "minecraft:item",
					"name": "windchimes:$id",
					"conditions": [
						{
							"condition": "minecraft:block_state_property",
							"block": "windchimes:$id",
							"properties": {
								"half": "upper"
							}
						}
					]
				}
			],
			"conditions": [
				{
					"condition": "minecraft:survives_explosion"
				}
			]
		}
	]
}
"@

	$recipePath = Join-Path $data "recipe\$id.json"
	if (-not (Test-Path $recipePath)) {
		Write-Json $recipePath @"
{
	"type": "minecraft:crafting_shaped",
	"category": "misc",
	"group": "long_wind_chime",
	"pattern": [
		" C ",
		"MMM"
	],
	"key": {
		"C": "windchimes:$shortId",
		"M": "$recipeMaterial"
	},
	"result": {
		"id": "windchimes:$id",
		"count": 1
	}
}
"@
	}

	Write-Advancement $id "windchimes:$shortId"

	$lang["block.windchimes.$id"] = "Long $name Wind Chime"
}

$langLines = $lang.GetEnumerator() | ForEach-Object { "`t" + (ConvertTo-Json $_.Key) + ': ' + (ConvertTo-Json $_.Value) }
Write-Json $langPath ("{`n" + ($langLines -join ",`n") + "`n}")

Write-Host "Generated assets for $($chimes.Count) wind chimes and $($longChimes.Count) long wind chimes."
