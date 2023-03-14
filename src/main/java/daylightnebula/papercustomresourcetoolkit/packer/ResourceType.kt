package daylightnebula.papercustomresourcetoolkit.packer

sealed class ResourceType<T: Resource> {
    object IMAGE : ResourceType<ImageResource>()
    object STATIC_MODEL : ResourceType<StaticModelResource>()
    object ANIMATED_MODEL : ResourceType<AnimatedModelResource>()
    object TEXT_IMAGE : ResourceType<TextImageResource>()
    object FONT : ResourceType<FontResource>()
}